/*
 * @Author: shawn
 * @LastEditTime: 2020-04-13 22:07:59
 */
let baidukey = "MOBBZ-DIYLX-53E4Z-TMVZF-YRHMZ-O5BUQ";
//  #ifdef MP
const QQMapWX = require("./qqmap-wx-jssdk.min");

export const getCity = (location, type) => {
  return new Promise((resolve, reject) => {
    // 实例化API核心类
    const qqmapsdk = new QQMapWX({
      key: baidukey, // 必填
    });
    console.log(location);
    let locationArray = location.split(",");
    console.log(locationArray);

    qqmapsdk.reverseGeocoder({
      location: {
        latitude: Number(locationArray[1]),
        longitude: Number(locationArray[0]),
      },
      success: function (res) {
        //成功后的回调
        console.log(res);
        var address = res.result;
        if (type === "area") {
          resolve([
            address.address_component.province,
            address.address_component.city,
            address.address_component.district,
          ]);
        } else {
          resolve([address.address_component.city]);
        }
      },
      fail: function (error) {
        reject();
        console.error(error);
      },
    });
  });
};
// #endif
// #ifdef H5

export const getCity = (location, type) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: "/cityjson?ie=utf-8",
      method: "GET",

      success: (res) => {
        if (res.statusCode === 200) {
          let ip = /((25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))/.exec(
            res.data
          );

          // 实例化API核心类
          uni.request({
            url: `/wsapp/location/v1/ip?ip=${ip[0]}&key=${baidukey}`,
            method: "GET",
            success: (res) => {
              console.log(res);
              if (res.statusCode === 200 && res.data.status === 0) {
                resolve([
                  res.data.result.ad_info.province,
                  res.data.result.ad_info.city,
                  res.data.result.ad_info.district,
                ]);
              }
            },
          });
        }
      },
    });
  });
};
//  #endif
