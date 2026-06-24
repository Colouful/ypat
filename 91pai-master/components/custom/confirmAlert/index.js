/*
 * @Author: shawn
 * @LastEditTime : 2020-01-13 21:22:23
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

export default {
  name: "confirmAlert",
  components: {
    tuiModal
  },
  props: {
    visiable: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '提示'
    },
    button: {
      type: Array,
      default: function() {
        return [
          {
            text: "取消",
            type: "red",
            plain: true //是否空心
          },
          {
            text: "确定",
            type: "red",
            plain: false
          }
        ];
      }
    }
  },
  data() {
    return {};
  },
  methods: {
    sure(e) {
      console.log(e);
      const { index } = e;
      if (index === 1) {
        this.$emit("sure");
      } else {
        this.$emit("cancle");
      }
    }
  }
};
