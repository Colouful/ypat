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
      displayDate: '',
      timer: null,
    }
  },
  beforeDestroy() {
    this.clear()
  },
  methods: {
    checkAndShow() {
      const displayDate = today()
      const lastShowDate = uni.getStorageSync(STORAGE_KEY)
      if (lastShowDate === displayDate) return
      this.displayDate = displayDate
      this.seconds = 3
      this.visible = true
      this.start()
    },
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
      if (this.displayDate) {
        uni.setStorageSync(STORAGE_KEY, this.displayDate)
      }
      this.visible = false
      this.clear()
    },
  },
}
