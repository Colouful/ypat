/*
 * @Author: shawn
 * @LastEditTime: 2019-12-14 23:17:56
 */

import localStorageObj from "@/common/localStorage";
const state = {
  userInfo: uni.getStorageSync(localStorageObj.userInfo) || {},
  isLogin: uni.getStorageSync(localStorageObj.userInfo) ? true : false
};
const getters = {};
const mutations = {
  setUserInfo(state, data) {
    uni.setStorageSync(localStorageObj.userInfo, data);
    state.isLogin = true;
    state.userInfo = data;
  },
  
  logout(state) {
    state.isLogin = false;
    state.userInfo = "";
  }
};
const actions = {};
export default {
  namespaced: true, //用于在全局引用此文件里的方法时标识这一个的文件名
  state,
  getters,
  mutations,
  actions
};
