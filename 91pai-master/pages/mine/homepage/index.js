/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 10:45:06
 */
import ypList from "@/components/custom/ypList/index.vue";
import { getUserInfo } from "@/common/utils";
export default {
  components: {
    ypList
  },
  data() {
    return {
      userInfo: {},
      winHeight: "",
      userId: "",
      navbar: [
        {
          name: "发布的约拍"
        },
        {
          name: "申请的约拍"
        }
      ],
      currentTab: "0"
    };
  },
  onLoad(options) {
    if (options && options.userId) {
      this.userId = options.userId;
    }
    uni.getSystemInfo({
      success: res => {
        let calc = res.windowHeight;
        this.winHeight = (calc - 0) * 2;
      }
    });
  },
  mounted() {
    this.getUserInfo();
  },
  methods: {
    previewImage: function(e) {
      uni.previewImage({
        current: 0,
        indicator: "none",
        urls: [this.userInfo.imgpath]
      });
    },
    async getUserInfo() {
      let res = await getUserInfo({ id: this.userId });
      this.userInfo = res;
    },
    changeTab(type) {
      this.currentTab = type + "";
      uni.pageScrollTo({
        scrollTop: 0,
        duration: 0
      });
      this.$refs.mychild.downRefresh(true, type);
    },
    fatherMethod() {
      console.log("father");
      uni.stopPullDownRefresh();
    }
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  // 刷新页面
  onPullDownRefresh() {
    console.log();
    this.$refs.mychild.downRefresh();
  },
  onReachBottom: function() {
    this.$refs.mychild.reachBottom();
  }
};
