/*
 * @Author: shawn
 * @LastEditTime: 2020-05-14 08:10:12
 */
import { getUserInfo } from "@/common/utils";
/**
 * @description: 首页 登录  我的
 * @param {type} 
 * @return: 
 */
export default (userInfo, router) => {
  if (!userInfo && router !== "home") {
    uni.reLaunch({
      url: "/pages/home/home/index",
    });
    return;
  }
  if (
    !userInfo.profess ||
    !userInfo.gender ||
    (userInfo.gender !== "2" && userInfo.gender !== "1") ||
    !userInfo.birthday ||
    !userInfo.province ||
    !userInfo.city
  ) {
    if (router === "login") {
      uni.redirectTo({
        url: "/pages/home/introduce/index",
      });
      return;
    } else {
      uni.navigateTo({
        url: "/pages/home/introduce/index",
      });
    }

    return;
  }
  if (router === "login") {
    uni.reLaunch({
      url: "/pages/home/home/index",
    });
  }
};

export const isNendUserInfo = (that) => {
  return new Promise(async (resolve, reject) => {
    let userInfo = await getUserInfo();
    if (
      !userInfo ||
      !userInfo.gender ||
      !userInfo.wx ||
      !userInfo.mobile ||
      !userInfo.nickname ||
      !userInfo.imgpath ||
      (userInfo.gender !== "2" && userInfo.gender !== "1")
    ) {
      that.tui.toast("请先个人完善信息");
      setTimeout(() => {
        uni.navigateTo({ url: "/pages/mine/userInfo/index" });
        reject(false);
      }, 2000);
    } else {
      resolve(true);
    }
  });
};
