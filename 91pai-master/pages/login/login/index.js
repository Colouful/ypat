/*
 * @Author: shawn
 * @LastEditTime: 2020-04-08 22:39:52
 */
import tuiButton from "@/components/thor-ui/button/button";
import { user_code, bd_code } from "@/common/vmeitime-http";
import localStorageObj from "@/common/localStorage";

export default {
  components: {
    tuiButton
  },
  data() {
    return {
      visible: false
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  methods: {
    async login(e) {
      const errMsg = e && e.detail && e.detail.errMsg;
      this.visible = true;

      try {
        if (errMsg !== "getUserInfo:ok") {
          this.showAuthDenyModal();
          return;
        }

        const provider = await this.getOauthProvider();
        const loginRes = await this.loginByProvider(provider);
        if (!loginRes.code) {
          throw new Error(loginRes.errMsg || "获取登录 code 失败");
        }

        const result = await this.getOpenidResult(loginRes.code);
        const openidInfo = this.parseOpenidResult(result);
        uni.setStorageSync(localStorageObj.openidInfo, {
          openid: openidInfo.openid,
          sessionKey: openidInfo.session_key
        });
        uni.redirectTo({
          url: `/pages/login/logininfo/index`
        });
      } catch (err) {
        this.tui.toast((err && (err.message || err.errMsg || err.msg)) || "登录失败，请稍后重试");
      } finally {
        this.visible = false;
      }
    },
    showAuthDenyModal() {
      uni.showModal({
        confirmColor: "#1BB6B6",
        cancelColor: "#B4B9B9",
        title: "温馨提示",
        showCancel: false,
        content: "您已拒绝授权，请重新点击并授权！",
        confirmText: "确定"
      });
    },
    getOauthProvider() {
      return new Promise((resolve, reject) => {
        uni.getProvider({
          service: "oauth",
          success(res) {
            const providers = Array.isArray(res.provider) ? res.provider : [res.provider];
            let provider = providers[0];
            /*  #ifdef MP-WEIXIN  */
            provider = providers.includes("weixin") ? "weixin" : provider;
            /* #endif */
            /*  #ifdef MP-BAIDU  */
            provider = providers.includes("baidu") ? "baidu" : provider;
            /* #endif */
            if (provider) {
              resolve(provider);
            } else {
              reject(new Error("未获取到登录服务商"));
            }
          },
          fail(err) {
            reject(err);
          }
        });
      });
    },
    loginByProvider(provider) {
      return new Promise((resolve, reject) => {
        uni.login({
          provider,
          success: resolve,
          fail: reject
        });
      });
    },
    async getOpenidResult(code) {
      let result = null;
      /*  #ifdef MP-WEIXIN  */
      result = await user_code({ code });
      /* #endif */
      /*  #ifdef MP-BAIDU  */
      result = await bd_code({ code });
      /* #endif */
      if (!result) {
        throw new Error("当前平台暂不支持登录");
      }
      return result;
    },
    parseOpenidResult(result) {
      if (!result || result.code !== 200 || !result.res) {
        throw new Error((result && result.msg) || "登录返回数据异常");
      }

      let openidInfo = null;
      try {
        openidInfo = JSON.parse(result.res);
      } catch (e) {
        throw new Error("登录返回数据异常");
      }

      if (openidInfo && openidInfo.errcode) {
        throw new Error("微信登录失败：" + (openidInfo.errmsg || openidInfo.errcode));
      }
      if (!openidInfo || !openidInfo.openid) {
        throw new Error("未获取到 openid，请检查后端 appid/secret");
      }
      return openidInfo;
    },
    navTo(url) {
      uni.redirectTo({
        url
      });
    }
  }
};
