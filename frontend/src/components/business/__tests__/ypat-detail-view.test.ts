// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { YpatInfo } from '@/api/types'
import YpatDetailView from '../YpatDetailView.vue'

const {
  getDetail,
  addFavorite,
  applyYpat,
  put,
  goRootTab,
  getCurrentPages,
  previewImage,
  navigateBack,
  navigateTo,
  reLaunch,
  setClipboardData,
  showToast,
  showModal,
} = vi.hoisted(() => ({
  getDetail: vi.fn(),
  addFavorite: vi.fn(),
  applyYpat: vi.fn(),
  put: vi.fn(() => Promise.resolve({})),
  goRootTab: vi.fn(),
  getCurrentPages: vi.fn(),
  previewImage: vi.fn(),
  navigateBack: vi.fn(),
  navigateTo: vi.fn(),
  reLaunch: vi.fn(),
  setClipboardData: vi.fn(),
  showToast: vi.fn(),
  showModal: vi.fn(),
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
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  beforeEach(() => {
    getDetail.mockReset()
    addFavorite.mockReset()
    applyYpat.mockReset()
    put.mockClear()
    goRootTab.mockClear()
    getCurrentPages.mockReset()
    previewImage.mockReset()
    navigateBack.mockReset()
    navigateTo.mockReset()
    reLaunch.mockReset()
    setClipboardData.mockReset()
    showToast.mockReset()
    showModal.mockReset()

    getCurrentPages.mockReturnValue([{ route: 'pages-sub/ypat/detail' }])
    vi.stubGlobal('getCurrentPages', getCurrentPages)

    const baseUni = (globalThis as typeof globalThis & {
      uni: Record<string, unknown>
    }).uni

    vi.stubGlobal('uni', {
      ...baseUni,
      previewImage,
      navigateBack,
      navigateTo,
      reLaunch,
      setClipboardData,
      showToast,
      showModal,
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

  it('作者摘要会将职业编码转换为职业标签，避免直接展示裸编码', async () => {
    const wrapper = await mountWithDetail(createDetail({
      userQo: {
        id: 214,
        nickname: '职业编码作者',
        profess: '0',
        realnameflag: '1',
        creditflag: '1',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    }))

    const authorCard = wrapper.find('.author-card')

    expect(authorCard.text()).toContain('摄影师')
    expect(authorCard.text()).not.toContain('0')
  })

  it('实名认证优先使用作者字段，并在作者字段无效或缺失时回退到约拍字段', async () => {
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

    const unknownFallbackWrapper = await mountWithDetail(createDetail({
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

    expect(unknownFallbackWrapper.find('.detail-trust-row').text()).toContain('已认证')
    expect(unknownFallbackWrapper.find('.author-card').text()).toContain('已认证')

    const emptyFallbackWrapper = await mountWithDetail(createDetail({
      realnameflag: '1',
      userQo: {
        id: 209,
        nickname: '空值回退认证',
        profess: '摄影师',
        realnameflag: '',
        creditflag: '1',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    }))

    expect(emptyFallbackWrapper.find('.detail-trust-row').text()).toContain('已认证')
    expect(emptyFallbackWrapper.find('.author-card').text()).toContain('已认证')

    const missingAuthorFlagDetail = createDetail({
      realnameflag: '1',
      userQo: {
        id: 210,
        nickname: '缺失回退认证',
        profess: '摄影师',
        creditflag: '1',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    })

    if (missingAuthorFlagDetail.userQo) delete missingAuthorFlagDetail.userQo.realnameflag

    const missingFallbackWrapper = await mountWithDetail(missingAuthorFlagDetail)

    expect(missingFallbackWrapper.find('.detail-trust-row').text()).toContain('已认证')
    expect(missingFallbackWrapper.find('.author-card').text()).toContain('已认证')
  })

  it('担保金优先使用约拍字段，并在约拍字段无效或缺失时回退到作者字段', async () => {
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

    const unknownFallbackWrapper = await mountWithDetail(createDetail({
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

    expect(unknownFallbackWrapper.find('.detail-trust-row').text()).toContain('已缴担保金')
    expect(unknownFallbackWrapper.find('.author-card').text()).toContain('已缴担保金')

    const emptyFallbackWrapper = await mountWithDetail(createDetail({
      creditflag: '',
      userQo: {
        id: 211,
        nickname: '空值回退担保金',
        profess: '摄影师',
        realnameflag: '1',
        creditflag: '1',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    }))

    expect(emptyFallbackWrapper.find('.detail-trust-row').text()).toContain('已缴担保金')
    expect(emptyFallbackWrapper.find('.author-card').text()).toContain('已缴担保金')

    const missingDetailFlag = createDetail({
      userQo: {
        id: 212,
        nickname: '缺失回退担保金',
        profess: '摄影师',
        realnameflag: '1',
        creditflag: '1',
        memberActive: false,
        memberLevel: '',
        imgpath: '',
      },
    })

    delete missingDetailFlag.creditflag

    const missingFallbackWrapper = await mountWithDetail(missingDetailFlag)

    expect(missingFallbackWrapper.find('.detail-trust-row').text()).toContain('已缴担保金')
    expect(missingFallbackWrapper.find('.author-card').text()).toContain('已缴担保金')
  })

  it('会员徽标按会员等级映射展示 PRO、无效值与空值时的默认 VIP 文案', async () => {
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

    const emptyLevelWrapper = await mountWithDetail(createDetail({
      userQo: {
        id: 213,
        nickname: '空等级会员作者',
        profess: '摄影师',
        realnameflag: '1',
        creditflag: '1',
        memberActive: true,
        memberLevel: '',
        imgpath: '',
      },
    }))

    expect(emptyLevelWrapper.find('.author-card__member').text()).toContain('VIP')
    expect(emptyLevelWrapper.find('.detail-trust-row').text()).toContain('VIP会员')
  })

  it('作者卡片与底部主页按钮都只触发主页跳转且不误调用收藏或报名接口', async () => {
    const wrapper = await mountWithDetail(createDetail())

    await wrapper.find('.author-card').trigger('tap')

    expect(navigateTo).toHaveBeenCalledTimes(1)
    expect(navigateTo).toHaveBeenCalledWith({ url: '/pages-sub/user/profile?id=201' })

    navigateTo.mockClear()

    const actionButtons = wrapper.findAll('.detail-actions__mini')
    expect(actionButtons).toHaveLength(2)

    await actionButtons[1].trigger('tap')

    expect(navigateTo).toHaveBeenCalledTimes(1)
    expect(navigateTo).toHaveBeenCalledWith({ url: '/pages-sub/user/profile?id=201' })
    expect(addFavorite).not.toHaveBeenCalled()
    expect(applyYpat).not.toHaveBeenCalled()
  })

  it('点击收藏只调用一次收藏接口并显示成功提示，不触发报名', async () => {
    addFavorite.mockResolvedValueOnce({})

    const wrapper = await mountWithDetail(createDetail())
    const actionButtons = wrapper.findAll('.detail-actions__mini')

    expect(actionButtons).toHaveLength(2)

    await actionButtons[0].trigger('tap')
    await flushPromises()

    expect(addFavorite).toHaveBeenCalledTimes(1)
    expect(addFavorite).toHaveBeenCalledWith(999, 101)
    expect(showToast).toHaveBeenCalledWith({ title: '收藏成功', icon: 'success' })
    expect(navigateTo).not.toHaveBeenCalled()
    expect(applyYpat).not.toHaveBeenCalled()
  })

  it('点击立即约拍进入填写信息页面，不再弹窗直接报名', async () => {
    const wrapper = await mountWithDetail(createDetail())

    await wrapper.find('.detail-actions__primary').trigger('tap')
    await flushPromises()

    expect(navigateTo).toHaveBeenCalledTimes(1)
    expect(navigateTo).toHaveBeenCalledWith({ url: '/pages-sub/work/apply?ypatId=101' })
    expect(showModal).not.toHaveBeenCalled()
    expect(applyYpat).not.toHaveBeenCalled()
    expect(addFavorite).not.toHaveBeenCalled()
  })
})
