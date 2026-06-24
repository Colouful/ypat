/*
 * @Author: shawn
 * @LastEditTime: 2019-10-26 14:51:52
 */
export default {
    name: "tuiToast",
    props: {
    },
    data() {
      return {
        timer: null,
        //是否显示
        visible: false,
        //显示标题
        title: "操作成功",
        //显示内容
        content: "",
        //是否有icon
        icon: false,
        imgUrl: ""
      };
    },
    methods: {
      show: function (options) {
        let {
          duration = 2000,
          icon = false
        } = options;
        clearTimeout(this.timer);
        this.visible = true;
        this.title = options.title || "";
        this.content = options.content || "";
        this.icon = icon;
        if (icon && options.imgUrl) {
          this.imgUrl = options.imgUrl
        }
        this.timer = setTimeout(() => {
          this.visible = false;
          clearTimeout(this.timer);
          this.timer = null;
        }, duration);
      },
      getWidth(icon, content) {
        let width = "auto";
        if (icon) {
          width = content ? '420rpx' : '360rpx'
        }
        return width
      }
    }
  }