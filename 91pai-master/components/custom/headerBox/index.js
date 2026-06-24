/*
 * @Author: shawn
 * @LastEditTime : 2020-01-01 16:34:09
 */
import { filter } from "@/mixins/filter";

export default {
  name: "headerBox",
  components: {},
  props: {
    item: {
      type: Object,
      default: function() {
        return {};
      }
    }
  },
  mixins: [filter],
  data() {
    return {};
  },
  methods: {
    navTo(url) {
      uni.navigateTo({
        url
      });
    }
  }
};
