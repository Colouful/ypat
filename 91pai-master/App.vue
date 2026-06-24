<!--
 * @Author: shawn
 * @LastEditTime: 2020-03-26 11:20:13
 -->
<script>
import localStorageObj from "./common/localStorage";

export default {
  onLaunch: function (options) {
    uni.hideTabBar()
    console.log('onLaunch',options)
    // 登录的时候加了一个参数channel,   0：微信，1：百度，2：PC
    if (options && options.query && options.query.channel) {
      uni.setStorageSync(localStorageObj.channel, options.query.channel);
    } else {
      // #ifdef MP-BAIDU
      uni.setStorageSync(localStorageObj.channel, '1');
      // #endif
      // #ifdef MP-WEIXIN
      uni.setStorageSync(localStorageObj.channel, '0');
      // #endif
    }
  },
  globalData: {
    channel: null
  },
  onLoad() {
    uni.hideTabBar()
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
/*每个页面公共css */
@import "./common/app.scss";
@import "./common/animation.scss";
</style>
