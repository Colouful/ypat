import { describe, expect, it } from 'vitest'
import {
  WORK_TAG_STYLE_CODES,
  getProfessDisplayName,
  getProfessOptions,
  getWorkTagStyleOptions,
  resolveWorkTagStyleName,
} from '../enums'

describe('admin enum consistency', () => {
  it('keeps work tag style codes in backend order', () => {
    expect(WORK_TAG_STYLE_CODES).toEqual([
      'qinglv',
      'shangwu',
      'minguo',
      'hanfu',
      'yunzhao',
      'ertong',
      'anhei',
      'qingxu',
      'yejing',
      'xiaoyuan',
      'zhuangrong',
      'gufeng',
      'taobao',
      'shishang',
      'hefu',
      'qipao',
      'hanxi',
      'oumei',
      'senxi',
      'shaonv',
      'baolilai',
      'qingxin',
      'hunli',
      'cosplay',
      'jiaopian',
      'heibai',
      'jishi',
      'rixi',
      'fugu',
    ])
  })

  it('builds twenty nine work tag style options', () => {
    expect(getWorkTagStyleOptions()).toHaveLength(29)
  })

  it('resolves work tag style names from code, name, legacy numeric value, and custom fallback', () => {
    expect(resolveWorkTagStyleName('qinglv')).toBe('情侣')
    expect(resolveWorkTagStyleName('情侣')).toBe('情侣')
    expect(resolveWorkTagStyleName('10')).toBe('日系')
    expect(resolveWorkTagStyleName('custom')).toBe('custom')
  })

  it('exposes six public profession options in backend order', () => {
    expect(getProfessOptions()).toEqual([
      { label: '商家', value: '6' },
      { label: '摄影师', value: '0' },
      { label: '化妆师', value: '2' },
      { label: '摄像师', value: '9' },
      { label: '修图师', value: '3' },
      { label: '模特', value: '1' },
    ])
  })

  it('keeps historical profession display names', () => {
    expect(getProfessDisplayName('8')).toBe('素人模特')
  })
})
