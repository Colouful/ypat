/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 08:10:51
 */
import homeItem from "@/components/custom/homeItem/index.vue";

import { my_ypat_app_list, my_ypat_pub_list } from "@/common/vmeitime-http";

export default {
  name: "ypList",
  components: {
    homeItem
  },
  props: {
    userid: {
      default: "",
      type: String
    }
  },
  data() {
    return {
      page: 0,
      size: 10,
      content: [],
      loading: false,
      pullUpOn: false,
      type: "0"
    };
  },
  mounted() {
    this.my_ypat_app_list();
  },
  methods: {
    async my_ypat_app_list() {
      let res = null;
      let param = {
        page: this.page,
        size: this.size
      };
      if (this.userid) {
        param.userid = this.userid;
        param.status = "2";
      }
      res =
        this.type === "0"
          ? await my_ypat_pub_list(param)
          : await my_ypat_app_list(param);
      console.log(res);
      if (res && res.code === 200 && res.res && res.res.content) {
        this.loading = false;
        if (this.page === 0) {
          this.content = res.res.content;
        } else if (res.res && res.res.content.length > 0) {
          this.content = [...this.content, ...res.res.content];
        }
        console.log(res.res.totalElements, this.content.length);
        this.pullUpOn =
          this.type === "0"
            ? res.res.totalElements === this.content.length
            : res.res.totals === this.content.length;
      }
      this.$emit("fatherMethod");
    },
    navTo(url) {
      if (!uni.getStorageSync(localStorageObj.token)) {
        url = "/pages/login/login/index";
      }
      uni.navigateTo({
        url
      });
    },
    // 刷新页面
    downRefresh(change, type) {
      this.page = 0;
      if (change) {
        this.content = [];
        this.type = type + '';
      }
      this.my_ypat_app_list();
    },
    reachBottom() {
      if (this.pullUpOn) return;
      this.loading = true;
      this.page++;
      this.my_ypat_app_list();
    }
  }
};
