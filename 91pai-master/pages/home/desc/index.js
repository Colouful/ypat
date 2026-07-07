/*
 * @Author: shawn
 * @LastEditTime: 2020-05-19 22:02:00
 */
import headerBox from "@/components/custom/headerBox/index.vue";
import tuiTag from "@/components/thor-ui/tag/tag";
import confirmAlert from "@/components/custom/confirmAlert/index.vue";
import localStorageObj from "@/common/localStorage";
import qrcodeAlert from "@/components/custom/qrcodeAlert/index.vue";
import poster from "@/components/custom/poster/index.vue";

import {
  ypat_get,
  ypat_yd_add,
  my_ypat_sc_add,
  my_ypat_head_list,
  ypat_audit,
  param_list,
  ypat_upRecom,
} from "@/common/vmeitime-http";
import { filter } from "@/mixins/filter";
import { getUserInfo } from "@/common/utils";
export default {
  components: {
    headerBox,
    tuiTag,
    confirmAlert,
    qrcodeAlert,
    poster,
  },
  mixins: [filter],
  data() {
    return {
      qrcodeAlertShow: false,
      skeletonShow: true,
      ta: "Ta",
      isSave: false,
      content: {},
      options: {},
      banner: [],
      scrollH: 0, //滚动总高度
      bannerIndex: 0,
      current: 0,
      userInfo: {},
      visiable: false,
      button: [],
      pathName: "",
      recContent: [],
      listType: "",
      showRealName: {},
    };
  },
  onLoad: function (options) {
    console.log(options);
    console.log(decodeURIComponent(options.scene));
    let obj = {};
    // #ifdef MP-WEIXIN
    obj = wx.getMenuButtonBoundingClientRect();
    // #endif
    // #ifdef MP-BAIDU
    obj = swan.getMenuButtonBoundingClientRect();
    // #endif
    // #ifdef MP-ALIPAY
    my.hideAddToDesktopMenu();
    // #endif
    if (options.scene) {
      this.ID = options.scene.split("-")[0];
      uni.setStorageSync(
        localStorageObj.recmobile,
        options.scene.split("-")[1]
      );
    } else {
      this.ID = options.id;
    }

    this.options = options;
    if (options.listType) {
      this.listType = options.listType;
    }
    console.log(this.options, "-------");
    // 设置分享手机号
    if (options && options.recmobile) {
      uni.setStorageSync(localStorageObj.recmobile, options.recmobile);
    }
    setTimeout(() => {
      uni.getSystemInfo({
        success: (res) => {
          this.width = obj.left || res.windowWidth;
          this.height = obj.top
            ? obj.top + obj.height + 8
            : res.statusBarHeight + 44;
          this.top = obj.top
            ? obj.top + (obj.height - 32) / 2
            : res.statusBarHeight + 6;
          this.scrollH = res.windowWidth;
        },
      });
    }, 50);
  },
  mounted() {
    this.ypat_get();
    this.ypat_yd_add();
    this.my_ypat_head_list();
    this.param_list();
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
    this.getUserInfo();
  },
  methods: {
    posterFun() {
      this.$refs.poster.shareFc();
    },
    confirmFun2(type) {
      const _this = this;
      uni.showModal({
        confirmColor: "#1BB6B6",
        cancelColor: "#B4B9B9",
        title: "温馨提示",
        content: `${
          type === "yes_recmobile" ? "确定上推荐?" : "确定取消推荐?"
        }`,
        confirmText: `确定`,
        success(res) {
          if (res.confirm) {
            switch (type) {
              case "yes_recmobile":
                _this.shenhe2("1");

                break;
              case "no_recmobile":
                _this.shenhe2("0");

                break;

              default:
                break;
            }
          }
        },
      });
    },
    confirmFun(type) {
      const _this = this;
      uni.showModal({
        confirmColor: "#1BB6B6",
        cancelColor: "#B4B9B9",
        title: "温馨提示",
        content: `${
          type === "yes-yes"
            ? "通过审核并上推荐"
            : type === "yes-no"
            ? "通过审核不上推荐"
            : "不通过审核"
        }?`,
        confirmText: `确定`,
        success(res) {
          if (res.confirm) {
            switch (type) {
              case "yes-yes":
                _this.shenhe("2", "1");

                break;
              case "yes-no":
                _this.shenhe("2", "0");

                break;
              case "no":
                _this.shenhe("3", "-");

                break;
              default:
                break;
            }
          }
        },
      });
    },

    async shenhe2(recomflag) {
      let obj = {
        id: this.ID,
        recomflag,
      };

      let res = await ypat_upRecom(obj);
      if (res && res.code === 200) {
        this.tui.toast("提交成功");
        setTimeout(() => {
          uni.navigateBack();
        }, 2000);
      }
    },
    async shenhe(flag, recomflag) {
      let obj = {
        id: this.ID,
        flag,
      };
      if (recomflag !== "-") {
        obj.recomflag = recomflag;
      }
      let res = await ypat_audit(obj);
      if (res && res.code === 200) {
        this.tui.toast("提交成功");
        setTimeout(() => {
          uni.navigateBack();
        }, 2000);
      }
    },
    goLinkway(item) {
      if (
        !this.content ||
        !this.content.userQo ||
        this.userInfo.id !== this.content.userQo.id
      ) {
        return;
      }
      uni.navigateTo({
        url: `/pages/home/linkway/index?id=${item.sendperid}&messid=${item.id}&linkwayflag=${item.linkwayflag}&type=0`,
      });
    },
    async my_ypat_head_list() {
      let res = await my_ypat_head_list({
        ypatid: this.ID,
      });
      if (res && res.code === 200) {
        this.recContent = res.res.content;
      }
    },
    sure() {
      this.visiable = false;
      uni.navigateTo({ url: this.pathName + "?ypatid=" + this.ID });
    },
    cancle() {
      this.visiable = false;
    },
    async getUserInfo() {
      let res = await getUserInfo();
      this.userInfo = res;
    },
    patstyleFunc(str) {
      if (str && str.indexOf(",") > -1) {
        return str.split(",");
      } else if (str) {
        return [str];
      } else {
        return [];
      }
    },
    getFee(type) {
      switch (type) {
        case 1:
        case 2:
          return "收费" + this.content.chargeamt + "元";
        default:
          return this.chargeway(this.content.chargeway);
      }
    },
    // 收藏
    async my_ypat_sc_add() {
      let res = null;
      res = await my_ypat_sc_add({
        ypatid: this.ID,
      });
      if (res && res.code === 200) {
        this.content.colflag = "1";
        this.tui.toast("收藏成功");
      } else if (res.code === 1006) {
        this.tui.toast("已收藏");
      } else {
        this.tui.toast(res.msg);
      }
    },

    // 阅读+1
    async ypat_yd_add() {
      let res = await ypat_yd_add({
        ypatid: this.ID,
      });
    },
    async ypat_get() {
      let res = null;
      res = await ypat_get({
        id: this.ID,
      });
      console.log(res);
      if (res && res.code === 200) {
        this.skeletonShow = false;
        this.content = res.res;
        if (res.res.userQo && res.res.userQo.gender) {
          this.ta =
            res.res.userQo.gender === "1"
              ? "他"
              : (res.res.userQo.gender === "2" && "她") || "Ta";
        }
      }
    },
    async param_list() {
      let res = await param_list();
      if (res && res.code === 200) {
        this.showRealName = res.res;
      }
    },
    orderShe() {
      if (!this.baiduTips()) {
        return;
      }
      if (!uni.getStorageSync(localStorageObj.token)) {
        uni.navigateTo({ url: "/pages/login/login/index" });
        return;
      }
      if (
        this.content.realnameflag === "1" &&
        this.userInfo &&
        this.userInfo.realnameflag === "0" &&
        this.userInfo.status !== "1"
      ) {
        this.visiable = true;
        this.pathName = "/pages/mine/realname/intro/index";
        this.button = [
          {
            text: "取消",
            type: "red",
            plain: true, //是否空心
          },
          {
            text: "去实名",
            type: "red",
            plain: false,
          },
        ];
        return;
      }
      if (
        this.content.realnameflag === "1" &&
        this.userInfo &&
        this.userInfo.realnameflag === "0" &&
        this.userInfo.status === "1"
      ) {
        this.tui.toast("您的实名认证正在审核中，审核通过可进行发起约拍");
        return;
      }
      if (
        this.content.creditflag === "1" &&
        this.userInfo &&
        this.userInfo.creditflag === "0"
      ) {
        this.visiable = true;
        this.pathName = "/pages/mine/credit/index";
        this.button = [
          {
            text: "取消",
            type: "red",
            plain: true, //是否空心
          },
          {
            text: "去缴纳",
            type: "red",
            plain: false,
          },
        ];
        return;
      }
      this.visiable = false;
      uni.navigateTo({
        url: "/pages/home/orderShe/index" + "?ypatid=" + this.ID,
      });
    },
    baiduTips(type) {
      // #ifdef H5
      this.qrcodeAlertShow = true;
      return;
      // #endif
      return true;
    },
    save() {
      if (!this.baiduTips()) {
        return;
      }
      if (!uni.getStorageSync(localStorageObj.token)) {
        uni.navigateTo({ url: "/pages/login/login/index" });
        return;
      }
      this.my_ypat_sc_add();
    },
    bannerChange: function (e) {
      this.bannerIndex = e.detail.current;
    },
    changeItem(index) {
      this.current = index;
      this.bannerIndex = index;
    },
    previewImage: function (e) {
      let index = e.currentTarget.dataset.index;
      uni.previewImage({
        current: this.content.pics[index],
        urls: this.content.pics,
      });
    },
  },
  //转发
  onShareAppMessage: function (res) {
    const _this = this;
    return {
      title: `${_this.ta}正在爱去拍【${_this.target(
        _this.content.target
      )}】，快去约拍${_this.ta}吧！`,
      imageUrl: _this.content && _this.content.pics[0],
      path: `/pages/home/desc/index?id=${_this.ID}&recmobile=${_this.userInfo.mobile}`,
      success: function (res) {
        _this.tui.toast("分享成功,邀请更多好友挣更多拍拍豆");
      },
    };
  },
};
