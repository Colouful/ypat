/*
 * @Author: shawn
 * @LastEditTime: 2020-04-08 22:48:37
 */
import headerBox from "@/components/custom/headerBox/index.vue";
import { filter } from "@/mixins/filter";
import { ypat_yd_add } from "@/common/vmeitime-http";

export default {
  name: "homeItem",
  components: {
    headerBox,
  },
  props: {
    type: {
      type: String,
      default: "",
    },
    item: {
      type: Object,
      default: {},
    },
  },
  data() {
    return {};
  },
  mixins: [filter],
  methods: {
    navTo(url) {
      console.log(this.type);
      if (this.type === "infoList") {
        url = url + `&listType=${this.type}`;
      }
      console.log(url);
      uni.navigateTo({
        url,
      });
    },
    // 阅读+1
    async ypat_yd_add() {
      let res = await ypat_yd_add({
        ypatid: this.item.id,
      });
    },
    previewImage(current) {
      this.ypat_yd_add();
      uni.previewImage({
        urls: this.item.pics,
        current,
        longPressActions: {
          itemList: ["发送给朋友", "保存图片", "收藏"],
          success: function (data) {
            console.log(
              "选中了第" +
                (data.tapIndex + 1) +
                "个按钮,第" +
                (data.index + 1) +
                "张图片"
            );
          },
          fail: function (err) {
            console.log(err.errMsg);
          },
        },
      });
    },
  },
};
