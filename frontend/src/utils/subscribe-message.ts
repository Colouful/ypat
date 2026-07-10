import { getTemplateIds } from '@/api/modules/content'
import type { TemplateIdItem } from '@/api/types'

export type SubscribeMessageScene = 'apply' | 'publish' | 'message'

export interface SubscribeMessageResult {
  supported: boolean
  requested: boolean
  accepted: string[]
  rejected: string[]
  error?: string
}

const SCENE_TEMPLATE_IDS: Record<SubscribeMessageScene, number[]> = {
  apply: [0],
  publish: [2],
  message: [0, 1, 2],
}

const MESSAGE_TEST_TEMPLATE_ID = '7nzQyG3qOBV5Vb6nYCkZHh7hhKJoHp_kHC4mX6vo6lg'

let templateCache: TemplateIdItem[] | null = null

export async function preloadMessageSubscribeTemplates(): Promise<void> {
  if (templateCache) return
  try {
    const res = await getTemplateIds()
    templateCache = res.data || []
  } catch {
    templateCache = null
  }
}

export async function requestMessageSubscribe(scene: SubscribeMessageScene): Promise<SubscribeMessageResult> {
  const api = (uni as unknown as {
    requestSubscribeMessage?: (options: {
      tmplIds: string[]
      success?: (res: Record<string, string>) => void
      fail?: (error: { errMsg?: string }) => void
    }) => void
  }).requestSubscribeMessage

  if (typeof api !== 'function') {
    return { supported: false, requested: false, accepted: [], rejected: [], error: '当前环境不支持订阅消息' }
  }

  const tmplIds = await resolveTemplateIds(scene)
  if (tmplIds.length === 0) {
    return { supported: true, requested: false, accepted: [], rejected: [], error: '未配置订阅模板' }
  }

  return new Promise((resolve) => {
    api({
      tmplIds,
      success: (res) => {
        const accepted = tmplIds.filter((id) => res[id] === 'accept')
        const rejected = tmplIds.filter((id) => res[id] && res[id] !== 'accept')
        const errMsg = res.errMsg && !res.errMsg.includes(':ok') ? res.errMsg : undefined
        resolve({ supported: true, requested: true, accepted, rejected, error: errMsg })
      },
      fail: (error) => {
        resolve({
          supported: true,
          requested: true,
          accepted: [],
          rejected: tmplIds,
          error: error?.errMsg || '订阅授权失败',
        })
      },
    })
  })
}

export function pickTemplateIds(scene: SubscribeMessageScene, templates: TemplateIdItem[]): string[] {
  if (scene === 'message') {
    return [MESSAGE_TEST_TEMPLATE_ID]
  }

  const ids = SCENE_TEMPLATE_IDS[scene] || []
  return ids
    .map((id) => templates.find((item) => item.id === id)?.value)
    .filter((value): value is string => Boolean(value))
    .slice(0, 3)
}

export function getSubscribeMessageToastTitle(result: SubscribeMessageResult): string {
  if (result.accepted.length > 0) return '已开启提醒'
  if (!result.supported) return result.error || '当前环境不支持订阅消息'
  if (!result.requested) return result.error || '未配置订阅模板'
  if (result.error?.includes('No template data return')) return '订阅模板未在微信后台配置'
  if (result.error?.includes('template id exist')) return '订阅模板未在微信后台配置'
  if (result.error?.includes('ban')) return '请在微信设置中开启订阅消息'
  if (result.error) return result.error
  if (result.rejected.length > 0) return '请在弹窗中勾选消息模板'
  return '未开启提醒'
}

async function resolveTemplateIds(scene: SubscribeMessageScene): Promise<string[]> {
  if (scene === 'message') {
    return pickTemplateIds(scene, [])
  }

  if (templateCache) {
    return pickTemplateIds(scene, templateCache)
  }
  try {
    const res = await getTemplateIds()
    templateCache = res.data || []
    return pickTemplateIds(scene, res.data || [])
  } catch {
    templateCache = null
    return []
  }
}
