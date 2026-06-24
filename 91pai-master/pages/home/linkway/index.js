/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 10:42:17
 */
/*
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:28:22
 */
import listCell from "@/components/thor-ui/list-cell/list-cell";
import confirmAlert from "@/components/custom/confirmAlert/index.vue";
import { user_linkway_get, mess_get } from "@/common/vmeitime-http";
import { getUserInfo } from "@/common/utils";
import localStorageObj from "@/common/localStorage";

const thorui = require("@/components/thor-ui/utils/clipboard.thorui.js");

export default {
  components: {
    listCell,
    confirmAlert
  },
  data() {
    return {
      id: "",
      messid: "",
      item: {},
      userInfo: {},
      info: {},
      messDesc: {},
      linkwayflag: "",
      visiable: false,
      button: [],
      pathName: "",
      type: ""
    };
  },
  onLoad(options) {
    console.log(options);
    this.id = options.id;
    this.messid = options.messid;
    this.linkwayflag = options.linkwayflag;
    this.type = options.type;
  },
  mounted() {
    if (this.type === "0") {
      this.mess_get("0");
    } else {
      this.mess_get("1");
      this.messDesc = uni.getStorageSync(localStorageObj.itemMsg);
    }
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
    if (this.linkwayflag === "1") {
      this.user_linkway_get();
    } else {
      this.getUserInfo();
    }
  },
  methods: {
    async getUserInfo() {
      let res = await getUserInfo();
      this.userInfo = res;
    },
    sure() {
      this.visiable = false;
      if (this.pathName === "sure") {
        this.user_linkway_get();
        return;
      }
      uni.navigateTo({ url: this.pathName });
    },
    navTo(url) {
      uni.navigateTo({ url });
    },
    cancle() {
      this.visiable = false;
    },
    clipboard(data) {
      thorui.getClipboardData(data, res => {
        // #ifdef H5
        if (res) {
          this.tui.toast("复制成功");
        } else {
          this.tui.toast("复制失败");
        }
        // #endif
      });
    },

    async mess_get(type) {
      let res = await mess_get({
        id: this.messid
      });
      if (res && res.code === 200 && type === "0") {
        this.messDesc = res.res;
      }
    },
    goSubmit() {
      if (this.linkwayflag === "1" || this.info.mobile) {
        return;
      } else {
        if (this.userInfo && this.userInfo.ppd < 3) {
          this.visiable = true;
          this.pathName = "/pages/mine/ppd/index";
          this.button = [
            {
              text: "取消",
              type: "red",
              plain: true //是否空心
            },
            {
              text: "去获取",
              type: "red",
              plain: false
            }
          ];
          return;
        }
        this.visiable = true;
        this.pathName = "sure";
        this.button = [
          {
            text: "取消",
            type: "red",
            plain: true //是否空心
          },
          {
            text: "确定查看",
            type: "red",
            plain: false
          }
        ];
      }
    },
    async user_linkway_get() {
      let res = await user_linkway_get({
        userid: this.id,
        messid: this.messid
      });
      if (res && res.code === 200) {
        this.info = res.res;
      } else {
        this.tui.toast(res.msg);
      }
    }
  }
};
