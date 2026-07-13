/**
 * 作品主题标签相关常量
 */
import type { WorkTag } from '@/api/types/work'

export const WORK_TAG_LIMIT = 5

/** 默认标签文案（仅用于对齐后端初始化字典，不生成可提交的假 ID） */
export const WORK_TAG_FALLBACK_OPTIONS = [
  { code: 'qinglv', name: '情侣', sortNo: 1 },
  { code: 'shangwu', name: '商务', sortNo: 2 },
  { code: 'minguo', name: '民国', sortNo: 3 },
  { code: 'hanfu', name: '汉服', sortNo: 4 },
  { code: 'yunzhao', name: '孕照', sortNo: 5 },
  { code: 'ertong', name: '儿童摄影', sortNo: 6 },
  { code: 'anhei', name: '暗黑', sortNo: 7 },
  { code: 'qingxu', name: '情绪', sortNo: 8 },
  { code: 'yejing', name: '夜景', sortNo: 9 },
  { code: 'xiaoyuan', name: '校园', sortNo: 10 },
  { code: 'zhuangrong', name: '妆容', sortNo: 11 },
  { code: 'gufeng', name: '古风', sortNo: 12 },
  { code: 'taobao', name: '淘宝', sortNo: 13 },
  { code: 'shishang', name: '时尚', sortNo: 14 },
  { code: 'hefu', name: '和服', sortNo: 15 },
  { code: 'qipao', name: '旗袍', sortNo: 16 },
  { code: 'hanxi', name: '韩系', sortNo: 17 },
  { code: 'oumei', name: '欧美', sortNo: 18 },
  { code: 'senxi', name: '森系', sortNo: 19 },
  { code: 'shaonv', name: '少女', sortNo: 20 },
  { code: 'baolilai', name: '宝丽来', sortNo: 21 },
  { code: 'qingxin', name: '清新', sortNo: 22 },
  { code: 'hunli', name: '婚礼', sortNo: 23 },
  { code: 'cosplay', name: 'cosplay', sortNo: 24 },
  { code: 'jiaopian', name: '胶片', sortNo: 25 },
  { code: 'heibai', name: '黑白', sortNo: 26 },
  { code: 'jishi', name: '纪实', sortNo: 27 },
  { code: 'rixi', name: '日系', sortNo: 28 },
  { code: 'fugu', name: '复古', sortNo: 29 },
] as const

export const WORK_TAGS_FALLBACK = WORK_TAG_FALLBACK_OPTIONS.map((item) => item.name)

export function resolveWorkTagOptions(tags: WorkTag[] | null | undefined): WorkTag[] {
  return tags && tags.length > 0 ? tags : []
}

function splitTagValues(value?: string): string[] {
  return (value || '')
    .split(/[,，]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function uniqueTagNames(names: string[]): string[] {
  return [...new Set(names)]
}

export function resolveYpatTopicTags(
  patstyle?: string,
  patstyleTxt?: string,
  tagOptions: WorkTag[] = [],
): string[] {
  const tagIds = splitTagValues(patstyle)
  if (tagIds.length && tagOptions.length) {
    const tagNameById = new Map(tagOptions.map((tag) => [String(tag.id), tag.name]))
    const resolvedNames = tagIds.map((id) => tagNameById.get(id)).filter((name): name is string => Boolean(name))
    if (resolvedNames.length) return uniqueTagNames(resolvedNames)
  }

  const fallbackNames = splitTagValues(patstyleTxt)
  if (fallbackNames.length) return uniqueTagNames(fallbackNames)

  return uniqueTagNames(tagIds.filter((value) => !/^\d+$/.test(value)))
}
