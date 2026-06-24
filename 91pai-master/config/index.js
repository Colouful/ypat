/*
 * @Author: shawn
 * @LastEditTime: 2019-12-08 09:49:35
 */
const { NODE_ENV } = process.env;
const config = {
  development: {
    imgUrl: "http://appapi.duoyi.cblog.top/storage/",
    apiUrl: "https://www.91qupaier.com"
  },
  production: {
    imgUrl: "http://appapi.duoyi.cblog.top/storage/",
    apiUrl: "https://www.91qupaier.com"
  }
};
export default config[NODE_ENV];
