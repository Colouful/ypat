/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 08:15:14
 */
import homeItem from "@/components/custom/homeItem/index.vue";

import { ypat_audit_list, my_ypat_pub_list } from "@/common/vmeitime-http";

export default {
  name: "infoList",
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
      skeletonShowCategory: true,
      type: "1"
    };
  },
  mounted() {
    this.ypat_audit_list();
  },
  methods: {
    async ypat_audit_list() {
      this.skeletonShowCategory = true;
      let res = null;
      let param = {
        page: this.page,
        size: this.size,
        status: this.type
      };
      res = await ypat_audit_list(param).catch(() => {
        this.skeletonShowCategory = false;
      });
      console.log(res);
      this.skeletonShowCategory = false;
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
        this.type = type + "";
      }
      this.ypat_audit_list();
    },
    reachBottom() {
      if (this.pullUpOn) return;
      this.loading = true;
      this.page++;
      this.ypat_audit_list();
    }
  }
};
