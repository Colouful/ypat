import { work_complain } from "@/common/vmeitime-http";

export default {
  data() {
    return {
      workId: "",
      reason: "",
      content: "",
      images: [],
      reasons: ["盗图", "欺诈", "色情", "不实信息", "骚扰", "其他"],
    };
  },
  onLoad(options) {
    this.workId = options.workId || "";
  },
  onShow() {
    this.tui.commonFunc();
  },
  methods: {
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
      if (!this.content && this.images.length === 0) {
        this.tui.toast("请填写投诉内容或上传证据截图");
        return;
      }
      const res = await work_complain({
        workId: this.workId,
        reason: this.reason,
        content: this.content,
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
