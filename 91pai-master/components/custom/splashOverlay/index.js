const STORAGE_KEY = 'splash_last_show_date'

function today() {
  const date = new Date()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

export default {
  name: 'splashOverlay',
  data() {
    return {
      visible: false,
      seconds: 3,
      timer: null,
    }
  },
  mounted() {
    const lastShowDate = uni.getStorageSync(STORAGE_KEY)
    if (lastShowDate === today()) return
    this.visible = true
    this.start()
  },
  beforeDestroy() {
    this.clear()
  },
  methods: {
    start() {
      this.clear()
      this.timer = setInterval(() => {
        this.seconds -= 1
        if (this.seconds <= 0) {
          this.close()
        }
      }, 1000)
    },
    clear() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    close() {
      uni.setStorageSync(STORAGE_KEY, today())
      this.visible = false
      this.clear()
    },
  },
}
