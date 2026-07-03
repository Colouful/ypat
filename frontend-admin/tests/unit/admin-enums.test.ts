import { describe, expect, it } from 'vitest'
import {
  YpatChargeWay,
  YpatPatstyle,
  YpatTarget,
  WorkStatus,
  getWorkStatusOptions,
  getYpatChargeWayOptions,
  getYpatPatstyleOptions,
  getYpatTargetOptions,
} from '@/constants/enums'

describe('后台发布枚举', () => {
  it('约拍对象应与后端值对齐', () => {
    expect(YpatTarget.PHOTOGRAPHER.value).toBe('0')
    expect(YpatTarget.MODEL.value).toBe('1')
    expect(YpatTarget.VIDEOGRAPHER.value).toBe('2')
    expect(YpatTarget.MERCHANT.value).toBe('3')
    expect(YpatTarget.MAKEUP.value).toBe('4')
    expect(YpatTarget.RETOUCHER.value).toBe('5')
    expect(getYpatTargetOptions().map((o) => o.value)).toEqual(['0', '1', '2', '3', '4', '5'])
  })

  it('约拍风格应与用户端顺序一致', () => {
    expect(getYpatPatstyleOptions().map((o) => o.label)).toEqual([
      '复古',
      'INS',
      '胶片',
      '少女',
      '暗黑',
      '情绪',
      '夜景',
      '欧美',
      '商务',
      '韩系',
      '日系',
      '情侣',
      '样片',
    ])
    expect(YpatPatstyle.INS.value).toBe('1')
  })

  it('收费方式应与后端值对齐', () => {
    expect(getYpatChargeWayOptions().map((o) => o.value)).toEqual(['0', '1', '2', '3'])
    expect(getYpatChargeWayOptions().map((o) => o.label)).toEqual(['希望互勉', '我要收费', '可付费', '费用协商'])
    expect(YpatChargeWay.FREE.value).toBe('0')
  })

  it('作品状态筛选不应包含暂存', () => {
    expect(getWorkStatusOptions().map((o) => o.value)).toEqual(['1', '2', '3', '4'])
    expect(getWorkStatusOptions().map((o) => o.label)).toEqual(['待审核', '审核通过', '审核未通过', '已下架'])
    expect(WorkStatus.OFFLINE.value).toBe('4')
  })
})
