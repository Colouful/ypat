/*
 * @Author: shawn
 * @LastEditTime: 2020-05-02 11:22:47
 */
import tuiButton from "@/components/thor-ui/button/button";
import { user_login, user_get, bd_login } from "@/common/vmeitime-http";
import { mapMutations } from "vuex";
import { getUserInfo, adminList } from "@/common/utils";
import getNextUrl from "@/common/getNextUrl";
import localStorageObj from "@/common/localStorage";

export default {
  components: {
    tuiButton,
  },
  data() {
    return {
      openid: "",
      session_key: "",
      userInfo: {},
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  onLoad() {
    this.openid = uni.getStorageSync(localStorageObj.openidInfo).openid;
    this.sessionKey = uni.getStorageSync(localStorageObj.openidInfo).sessionKey;
  },
  methods: {
    ...mapMutations("userInfo", ["setUserInfo"]),
    navTo(url) {
      //   if (!this.hasLogin) {
      //     url = "/pages/public/login";
      //   }
      uni.navigateTo({
        url,
      });
    },
    getPhoneNumber(e) {
      console.log(e, "111111111");
      const _this = this;
      const { errMsg, encryptedData, sessionKey, iv } = e.detail;
      if (errMsg === "getPhoneNumber:ok") {
        uni.getProvider({
          service: "oauth",
          success: function (res) {
            uni.getUserInfo({
              provider: res.provider,
              lang: "zh_CN",
              success: function (infoRes) {
                console.log(infoRes, "infoRes");
                _this.user_login({
                  encryptedData,
                  iv,
                  userInfo: infoRes.userInfo,
                });
              },
              fail(err) {
                console.log(err);
                _this.tui.toast("授权失败" + err.errMsg);
              },
            });
          },
        });
      } else if (errMsg === "getPhoneNumber:fail user deny") {
        uni.showModal({
          confirmColor: "#1BB6B6",
          cancelColor: "#B4B9B9",
          title: "温馨提示",
          showCancel: false,
          content: "您已拒绝授权，请重新点击并授权！",
          confirmText: "确定",
          success(res) {},
        });
      } else {
        _this.tui.toast("授权失败" + errMsg);
      }
    },
    async user_get() {
      let result = await user_get();
      if (result.code === 200) {
        console.log(result);
      } else {
      }
    },
    async user_login(obj) {
      let recmobile = "";
      if (uni.getStorageSync(localStorageObj.recmobile)) {
        recmobile = uni.getStorageSync(localStorageObj.recmobile);
      }
      let param = {
        openid: this.openid,
        nickname: obj.userInfo.nickName,
        // mobile: obj.mobile,
        recmobile,
        encryptedData: obj.encryptedData,
        sessionKey: this.sessionKey,
        iv: obj.iv,
        channel: uni.getStorageSync(localStorageObj.channel),
        gender: obj.userInfo.gender,
        imgpath: obj.userInfo.avatarUrl,
        province: obj.userInfo.province,
        city: obj.userInfo.city,
      };
      let result = {};
      /*  #ifdef MP-WEIXIN  */
      result = await user_login(param);
      /* #endif */
      /*  #ifdef MP-BAIDU  */
      result = await bd_login(param);
      /* #endif */

      if (result.code === 200) {
        console.log(result);
        uni.removeStorageSync(localStorageObj.recmobile);
        uni.setStorageSync(localStorageObj.token, result.res.token);
        if (adminList.includes(result.res.mobile)) {
          uni.setStorageSync(localStorageObj.role, "superAdmin");
        } else {
          uni.setStorageSync(localStorageObj.role, "user");
        }
        getUserInfo()
          .then((res) => {
            this.tui.toast("登录成功", 2000, true);
            getNextUrl(res, "login");
          })
          .catch((err) => {
            this.tui.toast("登录成功", 2000, true);
            getNextUrl(null, "login");
          });
      } else {
        this.tui.toast(result.msg);
      }
    },
  },
};
