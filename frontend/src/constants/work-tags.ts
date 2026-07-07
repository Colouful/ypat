/**
 * 作品主题标签相关常量
 */
import type { WorkTag } from '@/api/types/work'

export const WORK_TAG_LIMIT = 5

/** 默认标签文案（仅用于对齐后端初始化字典，不生成可提交的假 ID） */
export const WORK_TAGS_FALLBACK: string[] = [
  '情侣', '商务', '民国', '汉服', '孕照',
  '儿童摄影', '暗黑', '情绪', '夜景',
  '校园', '妆容', '古风', '淘宝', '时尚',
  '和服', '旗袍', '韩系', '欧美', '森系',
  '少女', '宝丽来', '清新', '婚礼',
]

export function resolveWorkTagOptions(tags: WorkTag[] | null | undefined): WorkTag[] {
  return tags && tags.length > 0 ? tags : []
}
