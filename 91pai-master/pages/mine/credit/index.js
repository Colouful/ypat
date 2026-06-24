/*
 * @Author: shawn
 * @LastEditTime: 2020-03-29 16:53:07
 */
import gridFour from "@/components/custom/gridFour/index.vue";
import { order_create } from "@/common/vmeitime-http";
import mPay from "@/common/mPay";
import qrcodeAlert from "@/components/custom/qrcodeAlert/index.vue";

export default {
  components: {
    gridFour,
    qrcodeAlert
  },
  data() {
    return {
      ischeck: false,
      visible: false,
      qrcodeAlertShow: false
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  methods: {
    baiduTips() {
      // #ifdef MP-BAIDU
      this.qrcodeAlertShow = true;
      return;
      // #endif
      return true;
    },
    async order_create() {
      if (!this.baiduTips()) {
        return;
      }
      if (!this.ischeck) {
        return;
      }
      let param = {
        type: "2",
        total_fee: 199
      };
      this.visible = true;
      let res = await order_create(param).catch(() => {
        this.visible = false;
      });
      if (res && res.code === 200) {
        this.visible = false;
        mPay({
          provider: "wxpay",
          timeStamp: res.res.timeStamp,
          nonceStr: res.res.nonceStr,
          packages: res.res.package,
          signType: res.res.signType,
          paySign: res.res.paySign,
          success: result => {
            this.visible = false;
            this.tui.toast("提交成功");
            setTimeout(() => {
              uni.navigateBack();
            }, 2000);
          },
          fail: err => {
            this.visible = false;
            console.log(err);
          }
        });
      } else {
        this.visible = false;
        this.tui.toast(res.msg);
      }
    },
    /**
     * 统一跳转接口,拦截未登录路由
     * navigator标签现在默认没有转场动画，所以用view
     */
    navTo(url) {
      //   if (!this.hasLogin || !url) {
      //     url = "/pages/login/login/index";
      //   }
      uni.navigateTo({
        url
      });
    },
    changeBox() {
      console.log("1");
      this.ischeck = !this.ischeck;
    }
  }
};
