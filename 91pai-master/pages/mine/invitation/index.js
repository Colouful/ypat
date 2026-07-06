/*
 * @Author: shawn
 * @LastEditTime: 2020-04-10 17:30:31
 */
import { my_frd_list } from "@/common/vmeitime-http";
import { mapState } from "vuex";

export default {
  data() {
    return {
      page: 0,
      size: 10,
      loading: false,
      pullUpOn: false,
      invitation: [],
      totalElements: 0,
    };
  },
  computed: {
    ...mapState("userInfo", ["userInfo"]),
  },
  mounted() {
    this.my_frd_list();
  },
  methods: {
    async my_frd_list() {
      let res = await my_frd_list({
        page: this.page,
        size: this.size,
      });
      if (res && res.code === 200) {
        this.loading = false;
        if (this.page === 0) {
          this.invitation = res.res.content;
        } else if (res.res && res.res.content.length > 0) {
          this.invitation = [...this.invitation, ...res.res.content];
        }
        this.totalElements = res.res.totalElements;
        this.pullUpOn = this.invitation.length === res.res.totalElements;
      }
      uni.stopPullDownRefresh();
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
        title: `好友【${this.userInfo.nickname}】邀你一起来爱去拍，拍出最美的你`,
        /* #ifdef MP-BAIDU */
        imageUrl: "/static/images/poster/shareimg.png",
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
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  // 刷新页面
  onPullDownRefresh: function () {
    this.page = 0;
    this.my_frd_list();
  },
  onReachBottom: function () {
    if (this.pullUpOn) return;
    this.loading = true;
    this.page++;
    this.my_frd_list();
  },
};
