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
    login(e) {
      const { errMsg } = e.detail;
      const _uni = uni;
      const _this = this;
      _this.visible = true;
      uni.getProvider({
        service: "oauth",
        success: function(res) {
          uni.login({
            provider: res.provider,
            async success(res) {
              if (res.code) {
                let result = {};
                /*  #ifdef MP-WEIXIN  */
                result = await user_code({ code: res.code }).catch(() => {
                  _this.visible = false;
                });
                /* #endif */
                /*  #ifdef MP-BAIDU  */
                result = await bd_code({ code: res.code }).catch(() => {
                  _this.visible = false;
                });
                /* #endif */

                if (result.code === 200 && result.res) {
                  _this.visible = false;
                  let openidInfo = JSON.parse(result.res);
                  if (errMsg === "getUserInfo:ok") {
                    _this.visible = false;
                    uni.setStorageSync(localStorageObj.openidInfo, {
                      openid: openidInfo.openid,
                      sessionKey: openidInfo.session_key
                    });
                    _uni.redirectTo({
                      url: `/pages/login/logininfo/index`
                    });
                  } else if (errMsg === "getUserInfo:fail auth deny") {
                    console.log(errMsg, "--------");
                    // 定位权限未开启，引导设置
                    _uni.showModal({
                      confirmColor: "#1BB6B6",
                      cancelColor: "#B4B9B9",
                      title: "温馨提示",
                      showCancel: false,
                      content: "您已拒绝授权，请重新点击并授权！",
                      confirmText: "确定",
                      success(res) {
                        console.log(res, "000000000");
                        if (res.confirm) {
                        }
                      }
                    });
                    _this.visible = false;
                  } else {
                    _this.visible = false;
                  }
                } else {
                  _this.visible = false;
                  _this.tui.toast(result.msg);
                }
              } else {
                _this.visible = false;
                _this.tui.toast(res.errMsg);
              }
            },
            fail(err) {
              _this.visible = false;
              _this.tui.toast(err.errMsg);
            }
          });
        }
      });
    },
    navTo(url) {
      uni.redirectTo({
        url
      });
    }
  }
};
