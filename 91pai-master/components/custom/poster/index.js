import _app from "./app.js";
import { getSharePoster } from "./QS-SharePoster.js";
import { authorize } from "@/common/utils";
import { getShreUserPosterBackground } from "./QS-SharePoster";
import config from "@/config";
export default {
  name: "poster",
  props: {
    content: {
      type: Object,
      default: () => {
        return {};
      },
    },
    mobile: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      poster: {},
      curIndex: 0,
      curImg: "",
      qr_codeImg: "",
      qrShow: false,
      canvasId: "default_PosterCanvasId",
    };
  },
  mounted() {},
  computed: {
    qrCodeUrl() {
      return `${config.apiUrl}/qr/code?page=pages/home/desc/index&scene=${this.content.id}-${this.mobile}`;
    },
  },
  methods: {
    nextImg() {
      if (this.content.pics.length > 1) {
        if (this.curIndex + 1 < this.content.pics.length) {
          this.curIndex = this.curIndex + 1;
        } else {
          this.curIndex = 0;
        }
        this.shareFc();
      } else {
        this.tui.toast("无更多图片");
      }
    },
    async shareFc() {
      console.log(this.curIndex);
      this.curImg = this.content.pics[this.curIndex];
      let newbgObj = await getShreUserPosterBackground({
        backgroundImage: this.curImg,
        type: "testShareType",
      });
      let bottomHeight = newbgObj.width * 0.3;

      console.log(
        "------",
        newbgObj.width,
        newbgObj.height,
        newbgObj.width / newbgObj.height
      );
      const fontSize = newbgObj.width * 0.04;
      const fontSizeBig = newbgObj.width * 0.048;
      try {
        _app.log("准备生成:" + new Date());
        const d = await getSharePoster({
          _this: this, //若在组件中使用 必传
          type: "testShareType",
          backgroundImage: this.curImg,
          formData: {
            //访问接口获取背景图携带自定义数据
          },
          posterCanvasId: this.canvasId, //canvasId
          delayTimeScale: 20, //延时系数
          //  background: {
          // 	width: 1080,
          // 	height: 1920,
          // 	backgroundColor: '#666'
          // },
          bottomHeight,
          drawArray: ({ bgObj }) => {
            //可直接return数组，也可以return一个promise对象, 但最终resolve一个数组, 这样就可以方便实现后台可控绘制海报
            return new Promise((rs, rj) => {
              rs([
                {
                  type: "custom",
                  setDraw(Context) {
                    Context.setFillStyle("white");
                    Context.setGlobalAlpha(1);
                    Context.fillRect(
                      0,
                      bgObj.height + bottomHeight,
                      bgObj.width,
                      bottomHeight
                    );
                    Context.setGlobalAlpha(1);
                  },
                },
                {
                  type: "text",
                  text: `${this.content.userQo.nickname}/${this.content.city}`,
                  size: fontSize,
                  color: "#2C2D2D",
                  alpha: 1,
                  textAlign: "left",
                  fontFamily: "PingFangSC-Regular,PingFang SC",
                  textBaseline: "middle",
                  infoCallBack(textLength) {
                    return {
                      dx: newbgObj.width * 0.04,
                      dy: bgObj.height - bottomHeight + newbgObj.width * 0.06,
                    };
                  },
                },
                {
                  type: "text",
                  text: `${this.content.targetTxt} · ${this.content.chargewayTxt}`,
                  fontFamily: "PingFangSC-Medium,PingFang SC",
                  fontWeight: "bold",
                  size: fontSizeBig,
                  color: "#2C2D2D",
                  alpha: 1,
                  textAlign: "left",
                  textBaseline: "middle",
                  infoCallBack(textLength) {
                    return {
                      dx: newbgObj.width * 0.04,
                      dy: bgObj.height - bottomHeight + newbgObj.width * 0.14,
                    };
                  },
                },
                {
                  type: "image",
                  url: this.qrCodeUrl,
                  alpha: 1,
                  dx: bgObj.width - newbgObj.width * 0.18,
                  dy: bgObj.height - bottomHeight + newbgObj.width * 0.02,
                  infoCallBack(imageInfo) {
                    return {
                      dWidth: newbgObj.width * 0.14,
                      dHeight: newbgObj.width * 0.14,
                    };
                  },
                },
                {
                  type: "image",
                  url: "/static/images/poster/logo-img.png",
                  alpha: 1,
                  dx: (bgObj.width - newbgObj.width * 0.088 * 4) / 2,
                  dy: bgObj.height - bottomHeight + newbgObj.width * 0.22,
                  infoCallBack(imageInfo) {
                    return {
                      dWidth: newbgObj.width * 0.088 * 4,
                      dHeight: newbgObj.width * 0.011 * 4,
                    };
                  },
                },
              ]);
            });
          },
          setCanvasWH: ({ bgObj, type, bgScale }) => {
            // 为动态设置画布宽高的方法，
            this.poster = bgObj;
          },
        });
        _app.log(
          "海报生成成功, 时间:" +
            new Date() +
            "， 临时路径: " +
            d.poster.tempFilePath
        );
        this.poster.finalPath = d.poster.tempFilePath;
        this.qrShow = true;
      } catch (e) {
        _app.hideLoading();
        _app.showToast(JSON.stringify(e));
        console.log(JSON.stringify(e));
      }
    },
    async saveImage() {
      // #ifndef H5
      await authorize(
        "scope.writePhotosAlbum",
        () =>
          new Promise(() => {
            uni.saveImageToPhotosAlbum({
              filePath: this.poster.finalPath,
              success(res) {
                _app.showToast("保存成功", "success");
              },
            });
          }),
        "请开启相册权限"
      );
      // #endif
      // #ifdef H5
      var oA = document.createElement("a");
      oA.download = ""; // 设置下载的文件名，默认是'下载'
      oA.href = this.poster.finalPath;
      document.body.appendChild(oA);
      oA.click();
      oA.remove(); // 下载之后把创建的元素删除
      _app.showToast("保存成功", "success");
      // #endif
    },
    share() {
      // #ifdef APP-PLUS
      _app.getShare(
        false,
        false,
        2,
        "",
        "",
        "",
        this.poster.finalPath,
        false,
        false
      );
      // #endif

      // #ifndef APP-PLUS
      _app.showToast("分享了");
      // #endif
    },
    hideQr() {
      this.qrShow = false;
    },
  },
};
