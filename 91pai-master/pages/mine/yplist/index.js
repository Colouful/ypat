/*
 * @Author: shawn
 * @LastEditTime : 2019-12-28 19:42:20
 */
import tuiListView from "@/components/thor-ui/list-view/list-view";
import tuiListCell from "@/components/thor-ui/list-cell/list-cell";
import homeItem from "@/components/custom/homeItem/index.vue";

import {
  my_ypat_sc_list,
  my_ypat_rec_list,
  my_ypat_pub_list
} from "@/common/vmeitime-http";
import { filter } from "@/mixins/filter";

export default {
  components: {
    tuiListCell,
    tuiListView,
    homeItem
  },
  data() {
    return {
      page: 0,
      size: 10,
      loading: false,
      pullUpOn: false,
      content: [],
      type: "pubtimes"
    };
  },
  mixins: [filter],
  onLoad(options) {
    this.type = options.type;
    if (options.name) {
      uni.setNavigationBarTitle({
        title: options.name
      });
    }
  },
  mounted() {
    this.my_ypat_sc_list();
  },
  methods: {
    async my_ypat_sc_list() {
      let res = null;
      let param = {
        page: this.page,
        size: this.size
      };
      switch (this.type) {
        case "pubtimes":
          res = await my_ypat_pub_list(param);
          break;
        case "rectimes":
          res = await my_ypat_rec_list(param);
          break;
        case "coltimes":
          res = await my_ypat_sc_list(param);
          break;

        default:
          break;
      }

      if (res && res.code === 200) {
        this.loading = false;
        if (this.page === 0) {
          this.content = res.res.content;
        } else if (res.res && res.res.content.length > 0) {
          this.content = [...this.content, ...res.res.content];
        }
        this.pullUpOn =
          this.content.length === res.res.totals ||
          this.content.length === res.res.totalElements;
      }
      uni.stopPullDownRefresh();
    }
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  // 刷新页面
  onPullDownRefresh: function() {
    this.page = 0;
    this.my_ypat_sc_list();
  },
  onReachBottom: function() {
    if (this.pullUpOn) return;
    this.loading = true;
    this.page++;
    this.my_ypat_sc_list();
  }
};
