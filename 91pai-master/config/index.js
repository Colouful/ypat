/*
 * @Author: shawn
 * @LastEditTime: 2019-12-08 09:49:35
 */
const { NODE_ENV } = process.env;
const localApiUrl = process.env.VUE_APP_API_URL || "http://127.0.0.1:8081";
const config = {
  development: {
    imgUrl: "http://appapi.duoyi.cblog.top/storage/",
    apiUrl: localApiUrl
  },
  production: {
    imgUrl: "http://appapi.duoyi.cblog.top/storage/",
    apiUrl: localApiUrl
  }
};
export default config[NODE_ENV];
