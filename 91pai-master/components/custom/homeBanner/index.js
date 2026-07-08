export default {
  name: 'homeBanner',
  props: {
    list: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      current: 0,
    }
  },
  computed: {
    visibleBanners() {
      return (this.list || []).filter((item) => item && item.imgpath)
    },
  },
  methods: {
    change(e) {
      this.current = e.detail.current
    },
    tapBanner(item) {
      const image = item && item.imgpath
      if (!image) return
      uni.previewImage({
        current: image,
        urls: this.visibleBanners.map((banner) => banner.imgpath),
      })
    },
  },
}
