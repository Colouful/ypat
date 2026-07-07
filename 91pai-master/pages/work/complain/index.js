import { work_complain, work_get } from "@/common/vmeitime-http";

export default {
  data() {
    return {
      workId: "",
      content: {},
      reason: "",
      complainContent: "",
      images: [],
      reasons: ["盗图", "欺诈", "色情", "不实信息", "骚扰", "其他"],
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
  },
  onShow() {
    this.tui.commonFunc();
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
    chooseImage() {
      uni.chooseImage({
        count: 3 - this.images.length,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          this.images = this.images.concat(res.tempFilePaths || []).slice(0, 3);
        },
      });
    },
    remove(index) {
      this.images.splice(index, 1);
    },
    preview(index) {
      uni.previewImage({
        current: this.images[index],
        urls: this.images,
      });
    },
    confirmBlack() {
      uni.showModal({
        title: "确认加入黑名单？",
        content: "加入黑名单后，对方将不能给你发送约拍请求和极速联系信息",
        confirmText: "确认拉黑",
        confirmColor: "#1989fa",
      });
    },
    async submit() {
      if (!this.reason) {
        this.tui.toast("请选择投诉原因");
        return;
      }
      if (!this.complainContent && this.images.length === 0) {
        this.tui.toast("请填写投诉内容或上传证据截图");
        return;
      }
      const res = await work_complain({
        workId: this.workId,
        reason: this.reason,
        content: this.complainContent,
        pics: this.images,
      }).catch(() => null);
      if (res && res.code === 200) {
        this.tui.toast("投诉已提交，平台会尽快处理");
        setTimeout(() => uni.navigateBack(), 800);
      } else {
        this.tui.toast((res && res.msg) || "提交失败，请稍后再试");
      }
    },
  },
};
