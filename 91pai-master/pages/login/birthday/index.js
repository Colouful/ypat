/*
 * @Author: shawn
 * @LastEditTime: 2020-04-15 14:08:02
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
      defaultValTime: "",
      date: "",
      dateStr: "",
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  created() {
    this.getDefaultValTime();
  },
  mounted() {
    this.$refs.time.show();
  },
  methods: {
    onConfirm(e) {
      console.log(e);
      this.date = e.result;
      this.dateStr = e.obj.year + "年" + e.obj.month + "月" + e.obj.day + "日";
    },
    show() {
      this.$refs.time.show();
    },
    getDefaultValTime() {
      let date = new Date();
      let dateYear = date.getFullYear(); //获取年
      let dateMonth =
        date.getMonth() + 1 > 9
          ? date.getMonth() + 1
          : "0" + (date.getMonth() + 1); //获取月
      let dateDate = date.getDate() > 9 ? date.getDate() : "0" + date.getDate(); //获取当日
      this.defaultValTime = dateYear + "-" + dateMonth + "-" + dateDate;
      this.dateStr = this.defaultValTime;
    },
    submit() {
      if (!this.date) {
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
            birthday: this.date,
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
