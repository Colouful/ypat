/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 10:44:00
 */
import { getUserInfo } from "@/common/utils";
import { user_upd } from "@/common/vmeitime-http";
import getNextUrl from "@/common/getNextUrl";
export default {
  components: {},
  data() {
    return {
      manType: "",
      visible: false
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  methods: {
    selectMan(type) {
      this.manType = type;
    },
    saveUserInfo() {
      if (!this.manType) {
        return;
      }
      this.visible = true;
      getUserInfo()
        .then(async res1 => {
          let res2 = await user_upd({
            ...res1,
            gender: this.manType
          }).catch(() => {
            this.visible = false;
          });
          if (res2 && res2.code === 200) {
            getUserInfo()
              .then(res3 => {
                this.visible = false;
                getNextUrl(res3);
              })
              .catch(() => {
                this.visible = false;
              });
          }
        })
        .catch(() => {
          this.visible = false;
        });
    },
    navTo(url) {
      uni.navigateTo({
        url
      });
    }
  }
};
