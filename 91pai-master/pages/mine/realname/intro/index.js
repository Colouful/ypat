export default {
  data() {
    return {
      options: {},
      benefits: [
        { icon: '约', title: '可约拍优质模特', desc: '优质模特须实名后可约拍' },
        { icon: '联', title: '可使用极速联系', desc: '专业客服一对一服务' },
        { icon: '刷', title: '可刷新约拍动态', desc: '刷新排名，获得更多曝光' },
        { icon: '标', title: '实名认证醒目标识', desc: '显著提高约拍成功率' },
      ],
    }
  },
  onLoad(options) {
    this.options = options || {}
  },
  onShow() {
    this.tui.commonFunc()
  },
  methods: {
    start() {
      const query = []
      if (this.options.ypatid) query.push(`ypatid=${this.options.ypatid}`)
      if (this.options.workId) query.push(`workId=${this.options.workId}`)
      const suffix = query.length ? `?${query.join('&')}` : ''
      uni.navigateTo({ url: `/pages/mine/realname/index${suffix}` })
    },
  },
}
