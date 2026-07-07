import { getUserInfo } from "@/common/utils";
import { work_quick_apply } from "@/common/vmeitime-http";

const SAFE_TIPS_KEY = "work_apply_safe_tips_until";

export default {
  data() {
    return {
      workId: "",
      reason: "",
      wx: "",
      userInfo: {},
    };
  },
  onLoad(options) {
    this.workId = options.workId || "";
    this.showSafeTips();
  },
  async onShow() {
    this.tui.commonFunc();
    this.userInfo = await getUserInfo().catch(() => ({}));
    this.wx = this.userInfo.wx || "";
  },
  methods: {
    showSafeTips() {
      const until = Number(uni.getStorageSync(SAFE_TIPS_KEY) || 0);
      if (until > Date.now()) return;
      uni.showModal({
        title: "安全防骗提醒",
        content: "①切勿独自与陌生人在室内约拍！②切勿私下发送私密隐私照片！③未见面先收费的合作，切勿相信！",
        confirmText: "我知道了",
        showCancel: true,
        cancelText: "近期不再提醒",
        confirmColor: "#ff5361",
        success: (res) => {
          if (res.cancel) {
            uni.setStorageSync(SAFE_TIPS_KEY, Date.now() + 7 * 24 * 60 * 60 * 1000);
          }
        },
      });
    },
    async submit() {
      if (!this.reason) {
        this.tui.toast("请输入约拍理由");
        return;
      }
      if (!this.userInfo.mobile && !this.wx) {
        this.tui.toast("请完善联系方式");
        return;
      }
      const res = await work_quick_apply({
        workId: this.workId,
        reason: this.reason,
        mobile: this.userInfo.mobile,
        wx: this.wx,
      }).catch(() => null);
      if (res && res.code === 200) {
        uni.redirectTo({ url: "/pages/home/success/index?status=98" });
      } else {
        this.tui.toast((res && res.msg) || "提交失败，请稍后再试");
      }
    },
  },
};
