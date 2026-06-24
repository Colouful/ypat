/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 10:47:16
 */
import tuiListView from "@/components/thor-ui/list-view/list-view";
import tuiListCell from "@/components/thor-ui/list-cell/list-cell";
import { my_ppd_list } from "@/common/vmeitime-http";
import { filter } from "@/mixins/filter";

export default {
  components: {
    tuiListCell,
    tuiListView
  },
  data() {
    return {
      page: 0,
      size: 10,
      loading: false,
      pullUpOn: false,
      content: []
    };
  },
  mixins: [filter],
  mounted() {
    this.my_ppd_list();
  },
  methods: {
    async my_ppd_list() {
      let res = await my_ppd_list({
        page: this.page,
        size: this.size
      });
      if (res && res.code === 200) {
        this.loading = false;
        if (this.page === 0) {
          this.content = res.res.content;
        } else if (this.page > 0 && res.res && res.res.content.length > 0) {
          this.content = [...this.content, ...res.res.content];
        }
          this.pullUpOn = this.content.length === res.res.totals;
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
    this.my_ppd_list();
  },
  onReachBottom: function() {
    if (this.pullUpOn) return;
    this.loading = true;
    this.page++;
    this.my_ppd_list();
  }
};
