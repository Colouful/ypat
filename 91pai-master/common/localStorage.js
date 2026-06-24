/*
 * @Author: shawn
 * @LastEditTime: 2020-04-10 08:00:07
 */
const prefix = "UNI_LOCAL";
const storage = {
  userInfo: `${prefix}_userInfo`, // 用户手机号信息
  token: `${prefix}_token`, // 登录凭证
  location: `${prefix}_location`, // 经纬度
  openid: `${prefix}_openid`, // openid
  recmobile: `${prefix}_recmobile`, // 邀请的手机号
  itemMsg: `${prefix}_itemMsg`, // 消息详情
  publishData: `${prefix}_publishData`, // 发布信息缓存
  role: `${prefix}_role`, // 角色
  publishDataAddress: `${prefix}_publishDataAddress`, // 地址
  channel: `${prefix}_channel`, // 注册渠道
  openidInfo: `${prefix}_openidInfo`, // 用户openid
  areaData: `${prefix}_areaData`, // 地区数据
};
export default storage;
// 退出不清除的信息
const logoutNotClear = ["channel", "location", "areaData"];
export const clearStorage = () => {
  for (const key in storage) {
    if (storage.hasOwnProperty(key) && !logoutNotClear.includes(key)) {
      console.log(key);
      uni.removeStorageSync(storage[key]);
    }
  }
};
