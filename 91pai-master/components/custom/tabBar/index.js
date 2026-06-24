/*
 * @Author: shawn
 * @LastEditTime: 2020-04-13 21:25:38
 */
import tuiTabbar from "@/components/thor-ui/tui-tabbar/tui-tabbar";
import localStorageObj from "@/common/localStorage";
import qrcodeAlert from "@/components/custom/qrcodeAlert/index.vue";

export default {
  components: {
    tuiTabbar,
    qrcodeAlert,
  },
  props: {
    //primary,warning,green,danger,white，black，gray
    current: {
      type: Number,
      default: 0,
    },
  },
  data() {
    return {
      qrcodeAlertShow: false,
      tabBar: [
        {
          pagePath: "/pages/home/home/index",
          text: "首页",
          iconPath: "/static/images/tabbar/home@2x.png",
          selectedIconPath: "/static/images/tabbar/home_ac@2x.png",
        },

        {
          pagePath: "/pages/home/publish/index",
          iconPath: "/static/images/tabbar/plus.png",
          hump: true,
          jump: "navigateTo",
          selectedIconPath: "/static/images/tabbar/plus.png",
        },
        {
          pagePath: "/pages/mine/mine/index",
          text: "我的",
          iconPath: "/static/images/tabbar/mine@2x.png",
          selectedIconPath: "/static/images/tabbar/mine_ac@2x.png",
          //   num: 2,
          //   isDot: true
          // "verify": true
        },
      ],
    };
  },
  methods: {
    baiduTips(type) {
      // #ifdef H5
      this.qrcodeAlertShow = true;
      return;
      // #endif
      return true;
    },
    tabbarSwitch(e) {
      if (!this.baiduTips()) {
        return;
      }
      console.log(e);
      if (this.current === e.index) {
        return;
      }
      if (e.hump && !uni.getStorageSync(localStorageObj.token)) {
        let url = "/pages/login/login/index";
        uni.navigateTo({
          url,
        });
      } else if (e.jump) {
        uni[e.jump]({
          url: e.pagePath,
        });
      } else {
        uni.switchTab({
          animationType: "pop-in",
          url: e.pagePath,
        });
      }
    },
  },
};
