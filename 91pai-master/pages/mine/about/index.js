/*
 * @Author: shawn
 * @LastEditTime: 2020-04-08 22:36:56
 */
import tuiListView from "@/components/thor-ui/list-view/list-view";
import tuiListCell from "@/components/thor-ui/list-cell/list-cell";
import tuiActionsheet from "@/components/thor-ui/actionsheet/actionsheet";
import localStorageObj, { clearStorage } from "@/common/localStorage";

export default {
  components: {
    tuiListView,
    tuiListCell,
    tuiActionsheet,
  },
  data() {
    return {
      showActionSheet: false,
      token: "",
      maskClosable: true,
      tips: "确认清空搜索历史吗？",
      itemList: [],
      color: "#9a9a9a",
      size: 26,
      isCancel: true,
      cellList: [
        {
          title: "版本号",
          desc: "1.0.0",
        },
        {
          title: "微信公众号",
          desc: "搜索关注“爱去拍”",
        },
        {
          title: "客服电话",
          desc: "010-53605277",
          click: true,
        },
      ],
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  mounted() {
    this.token = uni.getStorageSync(localStorageObj.token);
  },
  methods: {
    makePhoneCall(item) {
      if (item.click) {
        uni.makePhoneCall({
          phoneNumber: "010-53605277",
        });
      }
    },
    closeActionSheet: function () {
      this.showActionSheet = false;
    },
    itemClick: function (e) {
      let index = e.index;
      this.closeActionSheet();
      console.log(index);
      clearStorage();
      this.tui.toast("退出成功");
      setTimeout(() => {
        uni.reLaunch({
          url: "/pages/home/home/index",
        });
      }, 2000);
    },
    openActionSheet: function () {
      let itemList = [
        {
          text: "退出登录",
          color: "#e53a37",
        },
      ];
      let maskClosable = true;
      let tips = "退出登录会清除您的登录信息，确认退出吗？";
      let color = "#9a9a9a";
      let size = 26;
      let isCancel = true;
      setTimeout(() => {
        this.showActionSheet = true;
        this.itemList = itemList;
        this.maskClosable = maskClosable;
        this.tips = tips;
        this.color = color;
        this.size = size;
        this.isCancel = isCancel;
      }, 0);
    },
  },
};
