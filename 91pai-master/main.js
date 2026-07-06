/*
 * @Author: shawn
 * @LastEditTime: 2020-04-29 21:43:26
 */
import Vue from "vue";
import App from "./App";
import store from "./store";
import tuiToast from "@/components/thor-ui/extend/toast/toast";
import tuiTips from "@/components/thor-ui/extend/tips/tips";
import tuiSkeleton from "@/components/thor-ui/tui-skeleton/tui-skeleton";
import tuiLoadmore from "@/components/thor-ui/loadmore/loadmore";
import tuiNomore from "@/components/thor-ui/nomore/nomore";
import tuiIcon from "@/components/thor-ui/icon/icon";
import tuiLoading from "@/components/thor-ui/loading/loading";

Vue.config.productionTip = false;
// 全局挂载toast组件
Vue.component("tui-toast", tuiToast);
Vue.component("tui-skeleton", tuiSkeleton);
Vue.component("tui-tips", tuiTips);
Vue.component("tui-nomore", tuiNomore);
Vue.component("tui-loadmore", tuiLoadmore);
Vue.component("tui-icon", tuiIcon);
Vue.component("tui-loading", tuiLoading);
const tui = {
  commonFunc: function () {
    // #ifdef MP-BAIDU
    swan.setPageInfo({
      title: "爱去拍_个人与摄影师的约拍平台(找摄影师，找模特)",
      keywords: "麻豆、找模特、摄影约拍、找摄影师、爱去拍、摄影约拍、约拍、去哪约拍、摄影师",
      description:
        "爱去拍是一家专业做摄影师，模特约拍服务平台，找摄影师，找模特，约拍麻豆，随时随地，相约就约。",
      image: ["/static/images/poster/shareimg.png"],
    });
    // #endif
  },
  toast: function (text, duration, success) {
    uni.showToast({
      title: text,
      icon: success ? "success" : "none",
      duration: duration || 2000,
    });
  },
};

Vue.prototype.tui = tui;

Vue.prototype.$eventHub = Vue.prototype.$eventHub || new Vue();
Vue.prototype.$store = store;
App.mpType = "app";

const app = new Vue({
  store,
  ...App,
});
app.$mount();
