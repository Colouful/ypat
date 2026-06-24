/*
 * @Author: shawn
 * @LastEditTime: 2020-05-07 23:36:28
 */
import WPicker from "@/components/custom/picker/picker.vue";
import { getUserInfo } from "@/common/utils";
import { user_upd } from "@/common/vmeitime-http";
import getNextUrl from "@/common/getNextUrl";

export default {
  components: {
    WPicker,
  },
  data() {
    return {
      defaultRegion: [],
      defaultValTime: "",
      date: "",
      dateStr: "",
      province: "",
      city: "",
      area: "",
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  created() {},
  mounted() {
    this.$refs.area.show();
  },
  methods: {
    onConfirm(e) {
      console.log(e);
      debugger
      this.province = e.obj.province.label;
      this.city = e.obj.city.label;
      this.area = e.obj.area.label;
    },
    show() {
      this.$refs.area.show();
    },

    submit() {
      if (!this.province || !this.city) {
        return;
      }
      this.visible = true;
      getUserInfo()
        .then(async (res1) => {
          let res2 = await user_upd({
            ...res1,
            profess: this.activeIndex,
            nickName: res1.nickname,
            pics: res1.imgpath,
            birthday: res1.birthday,
            province: this.province,
            city: this.city,
            area: this.area,
          }).catch(() => {
            this.visible = false;
          });
          if (res2 && res2.code === 200) {
            getUserInfo()
              .then((res3) => {
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
        url,
      });
    },
  },
};
