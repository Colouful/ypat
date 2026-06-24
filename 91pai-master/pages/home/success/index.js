/*
 * @Author: shawn
 * @LastEditTime: 2020-06-07 16:19:53
 */
/*
 * @Author: shawn
 * @LastEditTime : 2019-12-20 06:55:50
 */
import { oauth_get, param_list } from "@/common/vmeitime-http";
import localStorageObj from "@/common/localStorage";
import { getUserInfo } from "@/common/utils";

export default {
  data() {
    return {
      type: {},
      status: "",
      msg: [
        {
          status: "1",
          type: "ing",
          title: "审核中",
          desc: "审核结果将通过服务通知推送通知您",
        },
        {
          status: "2",
          type: "success",
          title: "审核成功",
          desc: "您信息已被审核成功，赶紧发布约拍信息吧",
        },
        {
          status: "3",
          type: "fail",
          title: "审核失败",
          desc: "您提交的信息有误，请重新填写",
        },
        {
          status: "0",
          type: "fail",
          title: "审核失败",
          desc: "您提交的信息有误，请重新填写",
        },
        {
          status: "99",
          type: "success",
          title: "发布成功",
          desc: "审核成功后，您发布的信息将被其他人看到",
        },
        {
          status: "98",
          type: "success",
          title: "申请成功",
          desc: "申请成功后，对方将会很快看到您的申请",
        },
        {
          status: "97",
          type: "ing",
          title: "实名审核中",
          desc: "审核结果将通过服务通知推送通知您",
        },
        {
          status: "96",
          type: "fail",
          title: "实名审核失败",
          btnText: "重新认证",
          desc: "您提交的信息有误，请重新填写",
          goUrl: "/pages/mine/realname/index",
        },
        {
          status: "95",
          type: "success",
          title: "实名审核成功",
          desc: "您信息已被审核成功，赶紧发布约拍信息吧",
        },
      ],
      showObj: {},
      userInfo: {},
      showRealName: {},
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  onLoad(option) {
    this.status = option.status;
  },
  mounted() {
    if (uni.getStorageSync(localStorageObj.token) && this.status === "99") {
      this.getUserInfo();
      this.param_list();
    }
    if (this.status) {
      this.getObj(this.status);
      return;
    }
    if (uni.getStorageSync(localStorageObj.token)) {
      this.oauth_get();
    }
  },
  methods: {
    async param_list() {
      let res = await param_list();
      if (res && res.code === 200) {
        this.showRealName = res.res;
      }
    },
    async getUserInfo() {
      let res = await getUserInfo();
      if (res && res.status !== "2" && res.status !== "1") {
        this.userInfo = res;
      }
    },
    async oauth_get() {
      let res = await oauth_get();
      //  暂存-0，已提交\待审核-1，审核通过-2，审核未通过-3
      if (res && res.code === 200) {
        this.getObj(res.res.status);
      }
    },
    getObj(status) {
      this.msg.forEach((element) => {
        if (element.status === status) {
          this.showObj = element;
        }
      });
    },
    navTo(url, jump) {
      if (!uni.getStorageSync(localStorageObj.token)) {
        url = "/pages/login/login/index";
      }
      if (jump) {
        uni[jump]({
          url,
        });
        return;
      }
      uni.navigateTo({
        url,
      });
    },
    goHome() {
      if (this.showObj.goUrl) {
        uni.redirectTo({
          url: this.showObj.goUrl,
        });
      } else {
        uni.reLaunch({
          url: "/pages/home/home/index",
        });
      }
    },
  },
};
