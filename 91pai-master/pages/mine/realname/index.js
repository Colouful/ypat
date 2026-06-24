/*
 * @Author: shawn
 * @LastEditTime: 2020-03-29 16:50:03
 */
import tuiListCell from "@/components/thor-ui/list-cell/list-cell";
import tuiModal from "@/components/thor-ui/modal/modal";
import gridFour from "@/components/custom/gridFour/index.vue";
import tuiButton from "@/components/thor-ui/extend/button/button";
import mPay from "@/common/mPay";
var sourceType = [["camera"], ["album"], ["camera", "album"]];
var sizeType = [["compressed"], ["original"], ["compressed", "original"]];
import { oauth_add, order_create, oauth_ocr } from "@/common/vmeitime-http";
import qrcodeAlert from "@/components/custom/qrcodeAlert/index.vue";

import { getUserInfo } from "@/common/utils";

export default {
  components: {
    tuiListCell,
    tuiModal,
    gridFour,
    tuiButton,
    qrcodeAlert
  },
  data() {
    return {
      qrcodeAlertShow: false,
      modal: false,
      animation: true,
      imageList: [],
      sourceTypeIndex: 2,
      sourceType: ["拍照", "相册", "拍照或相册"],
      sizeTypeIndex: 2,
      sizeType: ["压缩", "原图", "压缩或原图"],
      certcode: "",
      name: "",
      visible: false,
      userInfo: {}
    };
  },
  onLoad() {
    this.baiduTips(true);
  },
  methods: {
    baiduTips(type) {
      // #ifdef MP-BAIDU
      this.qrcodeAlertShow = true;
      return;
      // #endif
      return true;
    },
    async oauth_ocr(base64) {
      let res = await oauth_ocr({
        cardfront: base64
      });
      if (res && res.code === 200) {
        this.name = res.res.name;
        this.certcode = res.res.certcode;
        this.$set(this.imageList, 0, base64);
      } else if (res && res.code === 1014) {
        this.$set(this.imageList, 0, base64);
      } else {
        this.tui.toast(res.msg);
      }
    },
    chooseImage: async function(type) {
      if (!this.baiduTips()) {
        return;
      }
      console.log(type);
      const _uni = uni;
      uni.chooseImage({
        sourceType: sourceType[this.sourceTypeIndex],
        sizeType: sizeType[this.sizeTypeIndex],
        count: 1,
        success: res1 => {
          console.log(res1, "0000");
          _uni.compressImage({
            src: res1.tempFilePaths[0],
            quality: 50,
            success: res => {
              // #ifdef MP
              this.toBase64(res.tempFilePath, type);
              // #endif
            }
          });

          //   // #ifdef APP-PLUS
          //   //提交压缩,因为使用了H5+ Api,所以自定义压缩目前仅支持APP平台
          //   var compressd = cp_images => {
          //     this.imageList = this.imageList.concat(cp_images); //压缩后的图片路径
          //   };
          //   image.compress(res.tempFilePaths, compressd);
          //   // #endif
        }
      });
    },
    toBase64(data, type) {
      uni.getFileSystemManager().readFile({
        filePath: data, //选择图片返回的相对路径
        encoding: "base64", //编码格式
        success: res => {
          //成功的回调
          let base64 = "data:image/jpeg;base64," + res.data; //不加上这串字符，在页面无法显示的哦
          switch (type) {
            case "image1":
              this.oauth_ocr(base64);
              break;
            case "image2":
              this.$set(this.imageList, 1, base64);
              break;
            case "image3":
              this.$set(this.imageList, 2, base64);
              break;

            default:
              break;
          }
        }
      });
    },
    close(type) {
      console.log(type);
      if (type === 0) {
        this.name = "";
        this.certcode = "";
      }
      this.$set(this.imageList, type, "");
    },
    getError() {
      if (!this.imageList[0]) {
        this.tui.toast("请上传身份证正面照片");
        return;
      }
      if (!this.imageList[1]) {
        this.tui.toast("请上传身份证反面照片");
        return;
      }
      if (!this.name) {
        this.tui.toast("请输入姓名");
        return;
      }
      if (
        !this.certcode ||
        !/(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(this.certcode)
      ) {
        this.tui.toast("请输入有效身份证号");
        return;
      }
      if (!this.imageList[2]) {
        this.tui.toast("请上传身份证手持照片");
        return;
      }

      return true;
    },
    async order_create() {
      this.visible = true;
      let res = await order_create({
        type: "1",
        total_fee: 29
      });
      if (res && res.code === 200) {
        mPay({
          provider: "wxpay",
          timeStamp: res.res.timeStamp,
          nonceStr: res.res.nonceStr,
          packages: res.res.package,
          signType: res.res.signType,
          paySign: res.res.paySign,
          success: result => {
            this.visible = false;
            this.publish();
            console.log(JSON.stringify(result));
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
    async publish() {
      if (!this.baiduTips()) {
        return;
      }
      let param = {};
      param.name = this.name;
      param.certcode = this.certcode;

      param.pics = this.imageList;
      console.log(this.imageList);
      let res = await oauth_add(param).catch(() => {
        this.visible = false;
      });
      if (res && res.code === 200) {
        this.visible = false;
        uni.redirectTo({
          url: "/pages/home/success/index?status=97"
        });
      } else {
        this.visible = false;
        this.tui.toast(res.msg);
      }
    },
    previewImage: function(e) {
      var current = e.target.dataset.src;
      let imageList = [];
      for (let index = 0; index < this.imageList.length; index++) {
        const element = this.imageList[index];
        if (element) {
          imageList.push(element);
        }
      }
      uni.previewImage({
        current: current,
        urls: imageList
      });
    },
    async showModal() {
      if (!this.baiduTips()) {
        return;
      }
      if (!this.getError()) {
        return;
      }
      this.userInfo = await getUserInfo();
      if (this.userInfo.status === "0") {
        this.modal = true;
      } else if (this.userInfo.status) {
        this.visible = true;
        this.publish();
      } else {
        this.tui.toast("系统异常，请稍后重试");
      }
    },
    hideModal() {
      this.modal = false;
    }
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  onUnload() {
    this.imageList = [];
    this.sourceTypeIndex = 2;
    this.sourceType = ["拍照", "相册", "拍照或相册"];
    this.sizeTypeIndex = 2;
    this.sizeType = ["压缩", "原图", "压缩或原图"];
  }
};
