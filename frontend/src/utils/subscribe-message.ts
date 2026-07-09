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
        resolve({ supported: true, requested: true, accepted, rejected })
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
  const ids = SCENE_TEMPLATE_IDS[scene] || []
  return ids
    .map((id) => templates.find((item) => item.id === id)?.value)
    .filter((value): value is string => Boolean(value))
    .slice(0, 3)
}

async function resolveTemplateIds(scene: SubscribeMessageScene): Promise<string[]> {
  try {
    const res = await getTemplateIds()
    return pickTemplateIds(scene, res.data || [])
  } catch {
    return []
  }
}
