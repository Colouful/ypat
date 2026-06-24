/*
 * @Author: shawn
 * @LastEditTime: 2020-04-10 17:31:05
 */
import { product_list, order_create } from "@/common/vmeitime-http";
import { arrayOrder, getUserInfo } from "@/common/utils";
import mPay from "@/common/mPay";
import qrcodeAlert from "@/components/custom/qrcodeAlert/index.vue";

export default {
  components: {
    qrcodeAlert,
  },
  data() {
    return {
      visible: false,
      activeItem: 78,
      rechargeList: [],
      userInfo: {},
      productid: 3,
      qrcodeAlertShow: false,
    };
  },
  mounted() {
    this.product_list();
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
    this.getUserInfo();
  },
  methods: {
    baiduTips(type) {
      // #ifdef MP-BAIDU
      this.qrcodeAlertShow = true;
      return;
      // #endif
      return true;
    },
    async getUserInfo() {
      let res = await getUserInfo();
      this.userInfo = res;
    },
    async order_create() {
      if (!this.baiduTips()) {
        return;
      }
      if (!this.activeItem || !this.productid) {
        return;
      }
      let param = {
        type: "0",
        productid: this.productid,
        total_fee: Number(this.activeItem),
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
          success: (result) => {
            this.visible = false;
            this.tui.toast("购买成功");
            setTimeout(() => {
              uni.navigateBack();
            }, 2000);
            console.log(JSON.stringify(result));
          },
          fail: (err) => {
            this.visible = false;
            console.log(err);
          },
        });
      } else {
        this.visible = false;
        this.tui.toast(res.msg);
      }
    },
    async product_list() {
      let res = await product_list({
        status: 0,
      });
      if (res && res.code === 200 && res.res && res.res.content) {
        this.rechargeList = arrayOrder(res.res.content, "currval");
      }
    },
    selectItem(item) {
      this.activeItem = item.currval;
      this.productid = item.id;
    },
    navTo(url) {
      //   if (!this.hasLogin) {
      //     url = "/pages/public/login";
      //   }
      uni.navigateTo({
        url,
      });
    },
  },
  //转发
  onShareAppMessage: function (res) {
    console.log(this.userInfo);
    const _this = this;
    if (res.from === "button") {
      return {
        title: `好友{${this.userInfo.nickname}}邀你一起来爱去拍，拍出最美的你`,
        /* #ifdef MP-BAIDU */
        imageUrl: "https://www.91qupaier.com/static/images/aiqupai.png",
        /* #endif */
        /* #ifdef MP-WEIXIN */
        imageUrl: "/static/images/mine/invite_bg.png",
        /* #endif */
        path:
          "/pages/mine/invitationdesc/index?recmobile=" + this.userInfo.mobile,
        success: function (res) {
          _this.tui.toast("分享成功,邀请更多好友挣更多拍拍豆");
        },
      };
    }
  },
};
