/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 10:46:17
 */
import msgTab from "@/components/custom/msgTab/index.vue";
import badge from "@/components/thor-ui/badge/badge";
import tuiNomore from "@/components/thor-ui/nomore/nomore";
import tuiLoadmore from "@/components/thor-ui/loadmore/loadmore";
import messageList from "@/components/custom/messageList/index.vue";
import {
  my_ypat_send_unread_count,
  my_ypat_rec_unread_count
} from "@/common/vmeitime-http";

import { urlEncodeUrl } from "@/common/utils";
export default {
  components: {
    msgTab,
    badge,
    tuiNomore,
    tuiLoadmore,
    messageList
  },
  data() {
    return {
      page: 0,
      size: 10,
      pullUpOn: true,
      loading: false,
      msgList: [],
      winHeight: "",
      count_rec: 0,
      count_send: 0,
      type: "",
      navbar: [
        {
          name: "发布的约拍"
        },
        {
          name: "申请的约拍"
        }
      ],
      currentTab: "0",
      tabList: [
        {
          name: "收到的约拍"
        },
        {
          name: "申请的约拍"
        }
      ]
    };
  },
  onLoad(options) {
    this.type = options.type;
    uni.getSystemInfo({
      success: res => {
        let calc = res.windowHeight;
        this.winHeight = (calc - 0) * 2;
      }
    });
    if (this.type) {
      uni.setNavigationBarTitle({
        title: "收到的约拍"
      });
    }
  },
  onShow() {
       //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
    if (this.type) {
      this.changeTab("0");
    } else {
      this.my_ypat_send_unread_count();
    }
  },
  methods: {
    async my_ypat_send_unread_count() {
      let res = await my_ypat_send_unread_count();
      let res2 = await my_ypat_rec_unread_count();
      if (res && res.code === 200 && res2 && res2.code === 200) {
        this.count_send = res.res;
        this.count_rec = res2.res;
        if (
          this.count_send &&
          this.count_send > 0 &&
          (!this.count_rec || this.count_rec <= 0)
        ) {
          this.changeTab("1");
        } else {
          this.changeTab("0");
        }
      }
    },
    navTo(url, data) {
      let a = urlEncodeUrl(data);
      //   if (!this.hasLogin) {
      //     url = "/pages/public/login";
      //   }
      uni.navigateTo({
        url: url + `?${a}`
      });
    },
    changeTab(type) {
      this.currentTab = type + "";
      this.$refs.mychild0.downRefresh(true, type);
    },
    fatherMethod() {
      uni.stopPullDownRefresh();
    }
  },
  // 刷新页面
  onPullDownRefresh() {
    this.$refs.mychild0.downRefresh();
  },
  onReachBottom: function() {
    this.$refs.mychild0.reachBottom();
  }
};
