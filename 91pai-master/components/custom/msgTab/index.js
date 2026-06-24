/*
 * @Author: shawn
 * @LastEditTime: 2019-10-25 07:49:24
 */
export default {
  name: "msgTab",
  components: {},
  props: {
    tabList: {
      type: Array,
      default: function() {
        return [{ name: "111" }];
      }
    }
  },
  data() {
    return {};
  },
  methods: {}
};
