import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import QueryUserList from '../index.vue'

const { getUserListMock } = vi.hoisted(() => ({
  getUserListMock: vi.fn(),
}))

vi.mock('@/api/modules/user', () => ({
  getUserList: getUserListMock,
}))

describe('查询系统用户列表', () => {
  beforeEach(() => {
    getUserListMock.mockResolvedValue({
      data: {
        content: [
          {
            id: 11,
            nickname: '摄影师阿明',
            mobile: '13800000000',
            genderTxt: '男',
            professTxt: '摄影师',
            province: '浙江省',
            city: '杭州市',
            area: '西湖区',
            ppd: 88,
            pubtimes: 2,
            rectimes: 3,
            coltimes: 4,
            regisdate: '2026-07-07 12:00:00',
            channelTxt: '微信小程序',
            statusTxt: '审核通过',
          },
        ],
        totalElements: 1,
      },
    })
  })

  it('应展示用户基础信息字段而不是实名审核专用字段', async () => {
    const wrapper = mount(QueryUserList, {
      global: {
        plugins: [ElementPlus],
      },
    })

    await flushPromises()

    const text = wrapper.text()
    expect(text).toContain('11')
    expect(text).toContain('摄影师阿明')
    expect(text).toContain('13800000000')
    expect(text).toContain('男')
    expect(text).toContain('摄影师')
    expect(text).toContain('浙江省 / 杭州市 / 西湖区')
    expect(text).toContain('微信小程序')
  })
})
