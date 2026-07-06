/**
 * 作品主题标签相关常量
 */
import type { WorkTag } from '@/api/types/work'

export const WORK_TAG_LIMIT = 5

/** 后端不可用或返回空列表时的 fallback 标签列表（对齐发布页设计枚举） */
export const WORK_TAGS_FALLBACK: string[] = [
  '情侣', '商务', '民国', '汉服', '孕照',
  '儿童摄影', '暗黑', '情绪', '夜景',
  '校园', '妆容', '古风', '淘宝', '时尚',
  '和服', '旗袍', '韩系', '欧美', '森系',
  '少女', '宝丽来', '清新', '婚礼',
]

export function createFallbackWorkTags(): WorkTag[] {
  return WORK_TAGS_FALLBACK.map((name, index) => ({
    id: index + 1,
    code: `fallback_${index + 1}`,
    name,
  }))
}

export function resolveWorkTagOptions(tags: WorkTag[] | null | undefined): WorkTag[] {
  return tags && tags.length > 0 ? tags : createFallbackWorkTags()
}
