/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 10:45:37
 */
import infoList from "@/components/custom/infoList/index.vue";
import { getUserInfo } from "@/common/utils";
export default {
  components: {
    infoList
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
      currentTab: "1"
    };
  },
  onShow(options) {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
    this.$refs.mychild.downRefresh();
  },
  mounted() {},
  methods: {
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
  // 刷新页面
  onPullDownRefresh() {
    console.log();
    this.$refs.mychild.downRefresh();
  },
  onReachBottom: function() {
    this.$refs.mychild.reachBottom();
  }
};
