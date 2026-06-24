/*
 * @Author: shawn
 * @LastEditTime: 2020-04-14 13:14:35
 */
/*
 * @Author: shawn
 * @LastEditTime : 2019-12-19 07:18:06
 */
import homeItem from "@/components/custom/homeItem/index.vue";
import { authorize, getLocation } from "@/common/utils";
import qrcodeAlert from "@/components/custom/qrcodeAlert/index.vue";

import {
  ypat_tj_list,
  ypat_tc_list,
  ypat_zx_list,
} from "@/common/vmeitime-http";
import localStorageObj from "@/common/localStorage";
import { getCity } from "@/common/getCity";
let timer = null;
export default {
  name: "ypList",
  components: {
    homeItem,
    qrcodeAlert,
  },
  props: {},
  data() {
    return {
      qrcodeAlertShow: false,
      skeletonShowCategory: true,
      page: 0,
      size: 10,
      status: "nodata",
      statusText: "暂无数据，赶紧去发布一条吧",
      content: [],
      homeDataDemo: [
        {
          name: "1",
          img: ["http://hkpic.crntt.com/upload/201408/13/103335831.jpg"],
        },
        {
          name: "1",
          img: ["http://hkpic.crntt.com/upload/201408/13/103335831.jpg"],
        },
      ],
      loading: false,
      pullUpOn: false,
      locationArray: [],
      type: "1",
    };
  },
  mounted() {
    // #ifdef H5
    this.ypat_tj_list();
    // #endif
  },
  methods: {
    async ypat_tj_list() {
      this.skeletonShowCategory = true;
      timer = setTimeout(() => {
        //   特殊处理
        if (this.skeletonShowCategory) {
          this.skeletonShowCategory = false;
          this.statusText = "请求失败，请重新加载";
          this.status = "refresh";
          this.$emit("fatherMethod");
        }
      }, 6000);
      let res = null;
      let param = {
        page: this.page,
        size: this.size,
        ...this.selectObj,
      };
      if (this.type === "0") {
        let location = "-1,-1";
        let city = "";
        // #ifdef MP
        location = await authorize(
          "scope.userLocation",
          () => getLocation(true),
          "请开启定位权限"
        ).catch((error) => {
          timer && clearTimeout(timer);
          location = "-1,-1";
          console.log("请开启定位权限", error);
          if (error === "gps") {
            this.status = "refresh";
            this.statusText = "请打开手机设置>开启GPS定位";
            this.$emit("fatherMethod");
          } else {
            this.status = "refresh";
            this.statusText = "请开启访问位置信息权限";
            this.$emit("fatherMethod");
          }
          this.skeletonShowCategory = false;
          this.content = [];
        });
        console.log("location", location);

        city =
          location &&
          location !== "-1,-1" &&
          (await getCity(location).catch(() => {
            timer && clearTimeout(timer);
            this.skeletonShowCategory = false;
          }));
        if (city && location && location !== "-1,-1" && city[0]) {
          param.city = city;
        } else {
          this.content = [];
          return;
        }
        // #endif
        // #ifdef H5
        city = await getCity().catch(() => {
          timer && clearTimeout(timer);
          this.skeletonShowCategory = false;
        });
        console.log(city);
        if (city && city[1]) {
          param.city = city[1];
        } else {
          this.content = [];
          return;
        }
        // #endif
      }
      switch (this.type) {
        case "0":
          // 同城
          res = await ypat_tc_list(param).catch(() => {
            this.skeletonShowCategory = false;
            timer && clearTimeout(timer);
            this.statusText = "请求失败，请重新加载";
            this.status = "refresh";
            this.$emit("fatherMethod");
          });
          break;
        case "1":
          // 推荐
          res = await ypat_tj_list({
            ...param,
            recomflag: "1",
          }).catch(() => {
            this.skeletonShowCategory = false;
            this.statusText = "请求失败，请重新加载";
            this.status = "refresh";
            this.$emit("fatherMethod");
            timer && clearTimeout(timer);
          });
          break;
        case "2":
          // 最新
          res = await ypat_zx_list(param).catch(() => {
            this.skeletonShowCategory = false;
            this.statusText = "请求失败，请重新加载";
            this.status = "refresh";
            this.$emit("fatherMethod");
            timer && clearTimeout(timer);
          });
          break;
        default:
          break;
      }
      this.skeletonShowCategory = false;
      if (res && res.code === 200 && res.res && res.res.content) {
        this.loading = false;
        if (this.page === 0) {
          this.content = res.res.content;
        } else if (res.res && res.res.content.length > 0) {
          this.content = [...this.content, ...res.res.content];
        }
        this.pullUpOn = res.res.totalElements === this.content.length;
      }
      this.$emit("fatherMethod");
    },
    baiduTips(type) {
      // #ifdef H5
      this.qrcodeAlertShow = true;
      return;
      // #endif
      return true;
    },
    navTo(url, type) {
      if (!this.baiduTips() && type) {
        return;
      }
      if (!uni.getStorageSync(localStorageObj.token)) {
        url = "/pages/login/login/index";
      }
      uni.navigateTo({
        url,
      });
    },
    setting() {
      uni.openSetting();
    },
    // 刷新页面
    downRefresh(change, obj) {
      this.page = 0;
      if (change) {
        if (obj.type === 0 || obj.type) {
          this.type = obj.type + "";
        } else {
          this.selectObj = obj;
        }
        this.content = [];
      }
      this.ypat_tj_list();
    },
    reachBottom() {
      if (this.pullUpOn) return;
      this.loading = true;
      this.page++;
      this.ypat_tj_list();
    },
  },
};
