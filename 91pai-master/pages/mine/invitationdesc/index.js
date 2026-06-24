/*
 * @Author: shawn
 * @LastEditTime: 2019-12-15 20:37:16
 */
import { isMobile } from "@/common/utils";
import localStorageObj from "@/common/localStorage";
export default {
  data() {
    return {};
  },
  onLoad(options) {
    if (options && options.recmobile && isMobile(options.recmobile)) {
      uni.setStorageSync(localStorageObj.recmobile, options.recmobile);
    }
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  methods: {
    goHome() {
      uni.reLaunch({
        url: "/pages/home/home/index"
      });
    }
  }
};
