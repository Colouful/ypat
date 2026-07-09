// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { YpatInfo } from '@/api/types'
import YpatDetailView from '../YpatDetailView.vue'

const { getDetail, addFavorite, applyYpat, put, goRootTab } = vi.hoisted(() => ({
  getDetail: vi.fn(),
  addFavorite: vi.fn(),
  applyYpat: vi.fn(),
  put: vi.fn(() => Promise.resolve({})),
  goRootTab: vi.fn(),
}))

vi.mock('@/api/modules/ypat', () => ({
  getDetail,
  addFavorite,
  applyYpat,
}))

vi.mock('@/api/request', () => ({
  put,
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    isLoggedIn: true,
    userInfo: { id: 999 },
  }),
}))

vi.mock('@/utils/tab-navigation', () => ({
  goRootTab,
}))

function createDetail(overrides: Partial<YpatInfo> = {}): YpatInfo {
  return {
    id: 101,
    describ: '周末一起拍一组质感人像',
    target: '1',
    patdate: '2026-07-12',
    chargeway: 'AA',
    chargewayTxt: '费用协商',
    city: '上海',
    area: '徐汇',
    creditflag: '1',
    realnameflag: '1',
    pattimes: 8,
    readtimes: 123,
    coltimes: 16,
    userid: 201,
    pics: ['/upload/cover-1.jpg', '/upload/cover-2.jpg'],
    targetTxt: '约模特',
    patstyleTxt: '复古,情绪',
    userQo: {
      id: 201,
      nickname: '长昵称会员摄影师阿泽',
      profess: '人像摄影师',
      realnameflag: '1',
      creditflag: '1',
      memberActive: true,
      memberLevel: 'PLUS',
      imgpath: '/upload/avatar.jpg',
    },
    ...overrides,
  }
}

async function mountWithDetail(detail: YpatInfo) {
  getDetail.mockResolvedValueOnce({ data: detail })

  const wrapper = mount(YpatDetailView, {
    props: { id: detail.id },
    global: {
      stubs: {
        swiper: { template: '<div><slot /></div>' },
        'swiper-item': { template: '<div><slot /></div>' },
      },
    },
  })

  await flushPromises()
  return wrapper
}

describe('YpatDetailView', () => {
  beforeEach(() => {
    getDetail.mockReset()
    addFavorite.mockReset()
    applyYpat.mockReset()
    put.mockClear()
    goRootTab.mockClear()
    const globalWithUni = globalThis as typeof globalThis & {
      getCurrentPages: () => unknown[]
      uni: Record<string, ReturnType<typeof vi.fn>>
    }

    globalWithUni.getCurrentPages = vi.fn(() => [{ route: 'pages-sub/ypat/detail' }])

    Object.assign(globalWithUni.uni, {
      previewImage: vi.fn(),
      navigateBack: vi.fn(),
      navigateTo: vi.fn(),
      reLaunch: vi.fn(),
      setClipboardData: vi.fn(),
      showToast: vi.fn(),
      showModal: vi.fn(),
    })
  })

  it('为已认证已缴担保金会员作者展示主信息信任状态和会员徽标', async () => {
    const wrapper = await mountWithDetail(createDetail())

    const trustRow = wrapper.find('.detail-trust-row')
    const authorCard = wrapper.find('.author-card')
    const authorMember = wrapper.find('.author-card__member')

    expect(trustRow.exists()).toBe(true)
    expect(wrapper.find('.detail-tag-row').text()).toContain('约模特')
    expect(wrapper.find('.detail-tag-row').text()).toContain('费用协商')
    expect(wrapper.find('.detail-tag-row').text()).toContain('上海·徐汇')
    expect(trustRow.text()).toContain('已认证')
    expect(trustRow.text()).toContain('已缴担保金')
    expect(trustRow.text()).toContain('会员')

    expect(authorCard.text()).toContain('长昵称会员摄影师阿泽')
    expect(authorCard.text()).toContain('已认证')
    expect(authorCard.text()).toContain('已缴担保金')
    expect(authorCard.text()).toContain('人像摄影师')
    expect(authorCard.text()).toContain('查看主页')

    expect(authorMember.exists()).toBe(true)
    expect(authorMember.text()).toContain('VIP+')
  })

  it('为未认证未缴担保金非会员作者展示负向状态且不显示会员徽标', async () => {
    const wrapper = await mountWithDetail(createDetail({
      creditflag: '0',
      realnameflag: '0',
      userQo: {
        id: 202,
        nickname: '普通用户',
        profess: '约拍爱好者',
        realnameflag: '0',
        creditflag: '0',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    }))

    const trustRow = wrapper.find('.detail-trust-row')
    const authorCard = wrapper.find('.author-card')

    expect(trustRow.exists()).toBe(true)
    expect(trustRow.text()).toContain('未认证')
    expect(trustRow.text()).toContain('未缴担保金')
    expect(trustRow.text()).toContain('非会员')

    expect(authorCard.text()).toContain('未认证')
    expect(authorCard.text()).toContain('未缴担保金')
    expect(authorCard.text()).toContain('约拍爱好者')
    expect(wrapper.find('.author-card__member').exists()).toBe(false)
  })

  it('实名认证优先使用作者字段，并在作者字段缺失时回退到约拍字段', async () => {
    const authorPriorityWrapper = await mountWithDetail(createDetail({
      realnameflag: '0',
      userQo: {
        id: 203,
        nickname: '作者优先认证',
        profess: '摄影师',
        realnameflag: '1',
        creditflag: '1',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    }))

    expect(authorPriorityWrapper.find('.detail-trust-row').text()).toContain('已认证')
    expect(authorPriorityWrapper.find('.author-card').text()).toContain('已认证')

    const fallbackWrapper = await mountWithDetail(createDetail({
      realnameflag: '1',
      userQo: {
        id: 204,
        nickname: '兼容回退认证',
        profess: '摄影师',
        realnameflag: 'unknown',
        creditflag: '1',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    }))

    expect(fallbackWrapper.find('.detail-trust-row').text()).toContain('已认证')
    expect(fallbackWrapper.find('.author-card').text()).toContain('已认证')
  })

  it('担保金优先使用约拍字段，并在约拍字段缺失时回退到作者字段', async () => {
    const detailPriorityWrapper = await mountWithDetail(createDetail({
      creditflag: '1',
      userQo: {
        id: 205,
        nickname: '约拍优先担保金',
        profess: '摄影师',
        realnameflag: '1',
        creditflag: '0',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    }))

    expect(detailPriorityWrapper.find('.detail-trust-row').text()).toContain('已缴担保金')
    expect(detailPriorityWrapper.find('.author-card').text()).toContain('已缴担保金')

    const fallbackWrapper = await mountWithDetail(createDetail({
      creditflag: 'unknown',
      userQo: {
        id: 206,
        nickname: '兼容回退担保金',
        profess: '摄影师',
        realnameflag: '1',
        creditflag: '1',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    }))

    expect(fallbackWrapper.find('.detail-trust-row').text()).toContain('已缴担保金')
    expect(fallbackWrapper.find('.author-card').text()).toContain('已缴担保金')
  })

  it('会员徽标按会员等级映射展示 PRO 与默认 VIP 文案', async () => {
    const proWrapper = await mountWithDetail(createDetail({
      userQo: {
        id: 207,
        nickname: 'PRO会员作者',
        profess: '摄影师',
        realnameflag: '1',
        creditflag: '1',
        memberActive: true,
        memberLevel: 'PRO',
        imgpath: '',
      },
    }))

    expect(proWrapper.find('.author-card__member').text()).toContain('PRO')
    expect(proWrapper.find('.detail-trust-row').text()).toContain('PRO会员')

    const defaultVipWrapper = await mountWithDetail(createDetail({
      userQo: {
        id: 208,
        nickname: '默认会员作者',
        profess: '摄影师',
        realnameflag: '1',
        creditflag: '1',
        memberActive: true,
        memberLevel: 'unknown',
        imgpath: '',
      },
    }))

    expect(defaultVipWrapper.find('.author-card__member').text()).toContain('VIP')
    expect(defaultVipWrapper.find('.detail-trust-row').text()).toContain('VIP会员')
  })
})
