import { getUserInfo } from "@/common/utils";
import { work_get, work_quick_apply } from "@/common/vmeitime-http";

const SAFE_TIPS_KEY = "work_apply_safe_tips_until";

export default {
  data() {
    return {
      workId: "",
      content: {},
      reason: "",
      wx: "",
      userInfo: {},
    };
  },
  computed: {
    author() {
      return this.content.user || this.content.userQo || {};
    },
    authorAvatar() {
      return this.author.imgpath || this.author.avatar || "/static/images/mine/mine_def_touxiang_3x.png";
    },
    authorName() {
      return this.author.nickname || this.author.name || "爱去拍用户";
    },
    authorMeta() {
      const profess = this.author.professTxt || this.author.professName || this.author.profess || "创作者";
      const city = this.author.city || this.content.city || "";
      return city ? `${profess} | ${city}` : profess;
    },
  },
  onLoad(options) {
    this.workId = options.workId || "";
    this.loadDetail();
    this.showSafeTips();
  },
  async onShow() {
    this.tui.commonFunc();
    this.userInfo = await getUserInfo().catch(() => ({}));
    this.wx = this.wx || this.userInfo.wx || "";
  },
  methods: {
    async loadDetail() {
      if (!this.workId) return;
      const res = await work_get({ id: this.workId }).catch(() => null);
      if (res && res.code === 200) {
        this.content = res.res || {};
      } else if (res && res.msg) {
        this.tui.toast(res.msg);
      }
    },
    goUserInfo() {
      uni.navigateTo({ url: "/pages/mine/userInfo/index" });
    },
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
      if (!this.reason || this.reason.trim().length < 6) {
        this.tui.toast("约拍理由不少于六个字");
        return;
      }
      if (!this.userInfo.mobile && !this.wx) {
        uni.showModal({
          title: "请完善联系方式",
          content: "请填写微信号，或前往个人信息页完善手机号后再提交约拍申请。",
          confirmText: "去完善",
          confirmColor: "#ff5361",
          success: (res) => {
            if (res.confirm) this.goUserInfo();
          },
        });
        return;
      }
      const res = await work_quick_apply({
        workId: this.workId,
        reason: this.reason.trim(),
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
