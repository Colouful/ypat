/*
 * @Author: shawn
 * @LastEditTime: 2020-05-25 15:11:57
 */
import tuiListCell from "@/components/thor-ui/list-cell/list-cell";
import tabBar from "@/components/custom/tabBar/index.vue";
import tuiListView from "@/components/thor-ui/list-view/list-view";
import getNextUrl from "@/common/getNextUrl";

import {
  my_ypat_unread_count,
  my_ypat_rec_list,
  tmplid_list,
  param_list,
} from "@/common/vmeitime-http";
import { getUserInfo, formatNum, sendMsg } from "@/common/utils";
import badge from "@/components/thor-ui/badge/badge";
import localStorageObj from "@/common/localStorage";
export default {
  components: {
    tuiListCell,
    tabBar,
    badge,
    tuiListView,
  },
  data() {
    return {
      getmsgCount: 0,
      visible: false,
      rectimesCount: 0,
      userInfodesc: {},
      msgCount: 0,
      showRealName: {},
      tmplidList: [],
      numList: [
        {
          key: "ppd",
          url: "ppd",
          name: "拍拍豆",
        },
        {
          key: "pubtimes",
          url: "yplist",
          name: "发布的约拍",
        },
        {
          key: "rectimesCount",
          url: "message",
          name: "收到的约拍",
        },
        {
          key: "coltimes",
          url: "yplist",
          name: "收藏的约拍",
        },
      ],
      cellList: [
        {
          title: "消息授权",
          line: true,
          role: "superAdmin",
          isNotNeedLogin: true,
          url: "messageAuth",
        },
        {
          title: "信息审核",
          line: true,
          role: "superAdmin",
          isNotNeedLogin: true,
          url: "/pages/mine/infoaudit/index",
        },
        // {
        //   title: "实名审核",
        //   line: true,
        //   role: "superAdmin",
        //   isNotNeedLogin: true,
        //   url: "realnameAudit"
        // }
        {
          dot: true,
          title: "我的消息",
          line: true,
          url: "/pages/mine/message/index",
        },
        {
          title: "我的主页",
          line: true,
          url: "/pages/mine/homepage/index",
        },
        {
          title: "好友邀请",
          desc: "赚拍拍豆",
          line: true,
          url: "/pages/mine/invitation/index",
        },
        {
          title: "关于我们",
          line: true,
          isNotNeedLogin: true,
          url: "/pages/mine/about/index",
        },
        {
          title: "帮助中心",
          line: true,
          isNotNeedLogin: true,
          url: "/pages/mine/helpcenter/index",
        },
      ],
      userInfo: {},
      role: "",
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
    if (uni.getStorageSync(localStorageObj.token)) {
      this.visible = true;
      getUserInfo()
        .then((res) => {
          this.visible = false;
          this.userInfo = res;
          getNextUrl(res, "mine");
        })
        .catch(() => {
          this.visible = false;
        });
      this.my_ypat_unread_count();
      this.my_ypat_rec_list();
    }
    this.role = uni.getStorageSync(localStorageObj.role);
  },
  onLoad() {
    uni.hideTabBar();
    this.tmplid_list();
    this.param_list();
  },

  methods: {
    async param_list() {
      let res = await param_list();
      if (res && res.code === 200) {
        this.showRealName = res.res;
      }
    },
    async tmplid_list() {
      let res = await tmplid_list();
      if (res && res.code === 200) {
        this.tmplidList = res.res;
      }
    },
    async my_ypat_rec_list() {
      let res = await my_ypat_rec_list();
      if (res && res.code === 200) {
        this.rectimesCount = res.res.totals;
      } else {
        this.rectimesCount = 0;
      }
    },

    formatNum(mobile) {
      return formatNum(mobile);
    },
    // 消息条数
    async my_ypat_unread_count() {
      let res = await my_ypat_unread_count();
      if (res && res.code === 200) {
        this.msgCount = res.res;
      }
      uni.stopPullDownRefresh();
    },
    /**
     * 统一跳转接口,拦截未登录路由
     * navigator标签现在默认没有转场动画，所以用view
     */
    navTo(url, type, item) {
      if (
        ((!item || !item.isNotNeedLogin) &&
          !uni.getStorageSync(localStorageObj.token)) ||
        !this.userInfo ||
        !this.userInfo.mobile ||
        !url
      ) {
        url = "/pages/login/login/index";
      }
      if (url === "messageAuth") {
        sendMsg(this.tmplidList, [3]);
        return;
      }
      if (type === "realname" && this.userInfo.status === "1") {
        url = "/pages/home/success/index?status=97";
      }
      if (type === "realname" && this.userInfo.status === "3") {
        url = "/pages/home/success/index?status=96";
      }
      if (type === "realname" && this.userInfo.status === "2") {
        url = "/pages/home/success/index?status=95";
      }
      if (type === "credit" && this.userInfo[type + "flag"] === "1") {
        this.tui.toast("已信用保证");
        return;
      }
      uni.navigateTo({
        url,
      });
    },
  },
  // 刷新页面
  onPullDownRefresh() {
    this.my_ypat_unread_count();
  },
};
