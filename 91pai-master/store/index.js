/*
 * @Author: shawn
 * @LastEditTime: 2019-11-30 15:46:18
 */
import Vue from "vue";
import Vuex from "vuex";

import userInfo from "./modules/userInfo";
Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    userInfo,
  }
});
