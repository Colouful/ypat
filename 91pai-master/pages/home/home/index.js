/*
 * @Author: shawn
 * @LastEditTime: 2020-05-15 13:49:39
 */
import tuiLoadmore from "@/components/thor-ui/loadmore/loadmore";
import tuiNomore from "@/components/thor-ui/nomore/nomore";
import tabBar from "@/components/custom/tabBar/index.vue";
import homeItem from "@/components/custom/homeItem/index.vue";
import tuiDrawer from "@/components/thor-ui/drawer/drawer";
import tuiSkeleton from "@/components/thor-ui/tui-skeleton/tui-skeleton";
import homeList from "@/components/custom/homeList/index.vue";
import { my_ypat_unread_count } from "@/common/vmeitime-http";
import { getUserInfo } from "@/common/utils";
import localStorageObj from "@/common/localStorage";
import demo from "@/components/custom/demo/index.vue";
import getNextUrl from "@/common/getNextUrl";

export default {
  components: {
    tuiLoadmore,
    tuiNomore,
    tabBar,
    homeItem,
    tuiDrawer,
    tuiSkeleton,
    homeList,
    demo,
  },
  data() {
    return {
      pagetc: 0,
      sizetc: 10,
      skeletonShow: true,
      scrollTop: 0,
      rightDrawer: false,
      currentTab: "1",
      loadding: false,
      selectObj: {},
      userInfo: {},
      //   winHeight: "",
      selectList: [
        {
          title: "职业",
          key: "profess",
          list: [
            {
              title: "不限",
              check: true,
              key: 999,
            },
            {
              title: "找模特",
              key: 1,
            },
            {
              title: "找摄影师",
              key: 0,
            },
          ],
        },
        {
          title: "性别",
          key: "gender",
          list: [
            {
              title: "不限",
              check: true,
              key: 999,
            },
            {
              title: "男",
              key: 1,
            },
            {
              title: "女",
              key: 2,
            },
          ],
        },
        {
          title: "实名认证",
          key: "realnameflag",
          list: [
            {
              title: "不限",
              check: true,
              key: 999,
            },
            {
              title: "是",
              key: 1,
            },
            {
              title: "否",
              key: 0,
            },
          ],
        },
        {
          title: "缴纳保证金",
          key: "creditflag",
          list: [
            {
              title: "不限",
              check: true,
              key: 999,
            },
            {
              title: "是",
              key: 1,
            },
            {
              title: "否",
              key: 0,
            },
          ],
        },
      ],
      homeDataDemo: [
        {
          name: "1",
          img: ["http://hkpic.crntt.com/upload/201408/13/103335831.jpg"],
        },
        {
          name: "1",
          img: ["http://hkpic.crntt.com/upload/201408/13/103335831.jpg"],
        },
      ],
      homeData: [],
      msgCount: 0,
    };
  },
  onLoad() {
    uni.hideTabBar();

    this.$refs.mychild && this.$refs.mychild.downRefresh(false);
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
    if (uni.getStorageSync(localStorageObj.token)) {
      this.my_ypat_unread_count();
      this.user_get();
    }
  },
  mounted() {},
  methods: {
    async user_get() {
      let res = await getUserInfo();
      this.userInfo = res;
      getNextUrl(res,'home');
    },

    checkItem(item, index, index2) {
      this.selectList[index].list.forEach((element) => {
        element.check = false;
      });
      this.selectList[index].list[index2].check = true;
      this.$set(this.selectList, index, this.selectList[index]);
    },
    reset() {
      this.selectList.forEach((element) => {
        element.list.forEach((element2, index2) => {
          if (index2 === 0) {
            element2.check = true;
          } else {
            element2.check = false;
          }
        });
      });
      this.selectList = [...this.selectList];
    },
    sure() {
      this.selectList.forEach((element) => {
        element.list.forEach((element2, index2) => {
          if (element2.check) {
            this.selectObj[element.key] = element2.key;
          }
        });
      });
      for (const key in this.selectObj) {
        if (this.selectObj.hasOwnProperty(key)) {
          if (this.selectObj[key] === 999) {
            delete this.selectObj[key];
          }
        }
      }
      this.rightDrawer = false;
      this.$refs.mychild.downRefresh(true, this.selectObj);
    },
    // 消息条数
    async my_ypat_unread_count() {
      let res = await my_ypat_unread_count();
      if (res && res.code === 200) {
        this.msgCount = res.res;
      }
    },
    changeTab(type) {
      this.goTop();
      this.currentTab = type + "";
      this.$refs.mychild.downRefresh(true, { type: type + "" });
    },
    fatherMethod() {
      console.log("father");
      uni.stopPullDownRefresh();
    },
    closeDrawer(e) {
      this.rightDrawer = false;
    },
    navTo(url) {
      //   if (!this.hasLogin || !url) {
      //     url = "/pages/login/login/index";
      //   }
      uni.navigateTo({
        url,
      });
    },
    rDrawer() {
      this.rightDrawer = true;
    },
    goTop() {
      uni.pageScrollTo({
        scrollTop: 0,
        duration: 0,
      });
    },
  },
  // 刷新页面
  onPullDownRefresh() {
    this.$refs.mychild.downRefresh(false);
  },
  onReachBottom: function () {
    this.$refs.mychild.reachBottom();
  },
  //转发
  onShareAppMessage: function (res) {
    const _this = this;
    let path = "";
    if (this.userInfo && this.userInfo.mobile) {
      path = `/pages/mine/invitationdesc/index?recmobile=${this.userInfo.mobile}`;
    } else {
      path = "/pages/home/home/index";
    }
    return {
      title: `爱去拍，约摄影师✖️约模特 随时随地 想约就约`,
      /* #ifdef MP-BAIDU */
      imageUrl: "https://www.91qupaier.com/static/images/aiqupai.png",
      /* #endif */
      /* #ifdef MP-WEIXIN */
      imageUrl: "/static/images/mine/invite_bg.png",
      /* #endif */
      path,
      success: function (res) {
        _this.tui.toast("分享成功");
      },
    };
  },
};
