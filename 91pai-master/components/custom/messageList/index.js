/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 08:15:32
 */
/*
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:25:00
 */
/*
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:01:43
 */

import { my_ypat_rec_list, my_ypat_send_list } from "@/common/vmeitime-http";
import listCell from "@/components/thor-ui/list-cell/list-cell";
import badge from "@/components/thor-ui/badge/badge";
import { mapState } from "vuex";
import localStorageObj from "@/common/localStorage";

export default {
  name: "messageList",
  components: {
    listCell,
    badge
  },
  props: {},
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
  computed: {
    ...mapState("userInfo", ["userInfo"])
  },
  mounted() {
    this.my_ypat_rec_list();
  },
  methods: {
    async my_ypat_rec_list() {
      let res = null;
      let param = {
        page: this.page,
        size: this.size
      };
      console.log(this.type);
      res =
        this.type === "0"
          ? await my_ypat_rec_list(param)
          : await my_ypat_send_list(param);
      if (res && res.code === 200 && res.res && res.res.content) {
        this.loading = false;
        if (this.page === 0) {
          this.content = res.res.content;
        } else if (res.res && res.res.content.length > 0) {
          this.content = [...this.content, ...res.res.content];
        }
        this.pullUpOn = res.res.totals === this.content.length;
      }
      this.$emit("fatherMethod");
    },
    navTo(url, item, index) {
      if (!uni.getStorageSync(localStorageObj.token)) {
        url = "/pages/login/login/index";
      }
      uni.setStorageSync(localStorageObj.itemMsg, item);
      uni.navigateTo({
        url: `${url}?id=${item.sendperid}&messid=${item.id}&linkwayflag=${item.linkwayflag}&type=${this.type}`
      });
    },
    // 刷新页面
    downRefresh(change, type) {
      this.page = 0;
      if (change) {
        this.content = [];
        this.type = type + "";
      }
      this.my_ypat_rec_list();
    },
    reachBottom() {
      if (this.pullUpOn) return;
      this.loading = true;
      this.page++;
      this.my_ypat_rec_list();
    }
  }
};
