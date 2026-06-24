/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 10:44:25
 */
import { professMap } from "@/common/dataMap";
import { getUserInfo } from "@/common/utils";
import { user_upd } from "@/common/vmeitime-http";
import getNextUrl from "@/common/getNextUrl";

export default {
  components: {
  },
  data() {
    return {
      activeItem: "",
      activeIndex: "",
      itemList: professMap,
      visible: false
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  methods: {
    submit() {
      if (this.activeIndex !== 0 && !this.activeIndex) {
        return;
      }
      this.visible = true;
      getUserInfo()
        .then(async res1 => {
          let res2 = await user_upd({
            ...res1,
            profess: this.activeIndex,
            nickName: res1.nickname,
            pics: res1.imgpath
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
    },
    selectItem(item, index) {
      this.activeIndex = index;
      this.activeItem = item;
    }
  }
};
