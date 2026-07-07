import { describe, expect, it } from 'vitest'
import { resolveWorkTagOptions, WORK_TAGS_FALLBACK, WORK_TAG_FALLBACK_OPTIONS } from '../work-tags'

describe('resolveWorkTagOptions', () => {
  it('does not create fake tag ids when backend returns an empty list', () => {
    const tags = resolveWorkTagOptions([])

    expect(tags).toEqual([])
  })

  it('keeps non-empty backend tags', () => {
    const backendTags = [{ id: 9, code: 'x', name: '胶片' }]

    expect(resolveWorkTagOptions(backendTags)).toBe(backendTags)
  })

  it('keeps backend default work tag fallback list aligned', () => {
    expect(WORK_TAGS_FALLBACK).toEqual([
      '情侣', '商务', '民国', '汉服', '孕照',
      '儿童摄影', '暗黑', '情绪', '夜景', '校园',
      '妆容', '古风', '淘宝', '时尚', '和服',
      '旗袍', '韩系', '欧美', '森系', '少女',
      '宝丽来', '清新', '婚礼', 'cosplay', '胶片',
      '黑白', '纪实', '日系', '复古',
    ])
    expect(WORK_TAG_FALLBACK_OPTIONS.map((item) => item.code)).toEqual([
      'qinglv', 'shangwu', 'minguo', 'hanfu', 'yunzhao',
      'ertong', 'anhei', 'qingxu', 'yejing', 'xiaoyuan',
      'zhuangrong', 'gufeng', 'taobao', 'shishang', 'hefu',
      'qipao', 'hanxi', 'oumei', 'senxi', 'shaonv',
      'baolilai', 'qingxin', 'hunli', 'cosplay', 'jiaopian',
      'heibai', 'jishi', 'rixi', 'fugu',
    ])
  })
})
