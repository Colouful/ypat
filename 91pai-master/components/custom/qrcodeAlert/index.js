/*
 * @Author: shawn
 * @LastEditTime: 2020-05-15 18:01:38
 */
/*
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:39:14
 */
/*
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:35:42
 */
import tuiModal from "@/components/thor-ui/modal/modal";
import { authorize } from "@/common/utils";

export default {
  name: "qrcodeAlert",
  components: {
    tuiModal,
  },
  props: {
    visiable: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
      default: "提示",
    },
  },
  data() {
    return {};
  },
  methods: {
    close() {
      this.$emit("callback");
    },
    async saveImg(img) {
      // #ifdef H5
      var oA = document.createElement("a");
      oA.download = ""; // 设置下载的文件名，默认是'下载'
      oA.href = img;
      document.body.appendChild(oA);
      oA.click();
      oA.remove(); // 下载之后把创建的元素删除
      // #endif
      // #ifdef MP
      await authorize(
        "scope.writePhotosAlbum",
        new Promise(() => {
          uni.getImageInfo({
            src: img,
            success: (image) => {
              console.log("图片信息：", JSON.stringify(image));
              uni.saveImageToPhotosAlbum({
                filePath: image.path,
                success: () => {
                  console.log("save success");
                  uni.showToast(
                    {
                      title: "图片保存成功",
                      duration: 2200,
                    },
                    () => {
                      this.$emit("callback");
                    }
                  );
                },
              });
            },
          });
        }),
        "请开启相册权限"
      );
      // #endif
    },
  },
};
