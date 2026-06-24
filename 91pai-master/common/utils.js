/*
 * @Author: shawn
 * @LastEditTime: 2020-04-10 07:43:10
 */
import localStorageObj from "./localStorage";
import { user_get } from "@/common/vmeitime-http";
import store from "@/store";
export const getUrl = (url, data) => {
  let params = "";
  if (data) {
    params = url + "?";
    for (const key in data) {
      params = params + `${key}=${encodeURIComponent(data[key])}&`;
    }
    params = params.slice(0, params.length - 1);
  } else {
    params = url;
  }
  return params;
};
export const formatNum = function (num) {
  //格式化手机号码
  if (isMobile(num)) {
    num = num.replace(/^(\d{3})\d{4}(\d{4})$/, "$1****$2");
  }
  return num;
};
export const compare = (obj1, obj2) => {
  var val1 = obj1.sort;
  var val2 = obj2.sort;
  if (val1 < val2) {
    return -1;
  } else if (val1 > val2) {
    return 1;
  } else {
    return 0;
  }
};

// 发布时间
// recordTime为提交的时间

export const releaseTime = (recordTime) => {
  const duration = (+new Date() - recordTime) / 1000;
  let displayTime = "";
  if (duration < 60) {
    displayTime = `${Math.round(Math.max(duration, 1))} 秒前`;
  } else if (duration < 60 * 60) {
    displayTime = `${Math.round(duration / 60)} 分钟前`;
  } else if (duration < 60 * 60 * 24) {
    displayTime = `${Math.round(duration / 60 / 60)} 小时前`;
  } else {
    displayTime = dateTime(recordTime);
  }
  return displayTime;
};
// 超过一天后显示发布日期
export const dateTime = (time) => {
  const times = new Date(time);
  const Y = times.getFullYear();
  let M = times.getMonth() + 1;
  M = M < 10 ? "0" + M : M;
  let D = times.getDate();
  D = D < 10 ? "0" + D : D;
  return `${Y}年-${M}月-${D}日`;
};

// scope.userInfo	uni.getUserInfo	用户信息
// scope.userLocation	uni.getLocation, uni.chooseLocation	地理位置
// scope.userLocationBackground	wx.userLocationBackground	后台定位	微信小程序
// scope.address	uni.chooseAddress	通信地址
// scope.record	uni.getRecorderManager	录音功能
// scope.writePhotosAlbum	uni.saveImageToPhotosAlbum, uni.saveVideoToPhotosAlbum	保存到相册	头条小程序的返回值是scope.album
// scope.camera	<camera /> 组件，头条下的扫码、拍照、选择相册	摄像头
// scope.invoice	wx.chooseInvoice	获取发票	微信小程序、QQ小程序
// scope.invoiceTitle	uni.chooseInvoiceTitle	发票抬头	微信小程序、百度小程序、QQ小程序
// scope.werun	wx.getWeRunData	微信运动步数	微信小程序

export const authorize = function (type, callBack, tips) {
  const _uni = uni;
  return new Promise((resove, reject) => {
    uni.authorize({
      scope: type,
      success(res) {
        console.log(res);
        if (res.errMsg === "authorize:ok" || res.errMsg === "授权成功") {
          callBack &&
            callBack()
              .then((resStr) => {
                console.log("成功返回");
                console.log(resStr);
                resove(resStr);
              })
              .catch(() => {
                console.log("未开启GPS");
                reject("gps");
                _uni.showModal({
                  confirmColor: "#1BB6B6",
                  cancelColor: "#B4B9B9",
                  title: "温馨提示",
                  content: "请打开手机设置>开启GPS定位",
                  confirmText: "确定",
                  success(res) {
                    if (res.confirm) {
                    }
                  },
                });
              });
        }
      },
      fail: function () {
        // 定位权限未开启，引导设置
        _uni.showModal({
          confirmColor: "#1BB6B6",
          cancelColor: "#B4B9B9",
          title: "温馨提示",
          content: tips,
          confirmText: "去设置",
          success(res) {
            console.log(res, "000000000");
            if (res.confirm) {
              //打开授权设置
              openSetting();
            }
          },
        });
        reject();
      },
    });
  });
};

// 获取地理位置
export const getLocation = (force) => {
  const _uni = uni;
  return new Promise((resove, reject) => {
    if (!uni.getStorageSync(localStorageObj.location) || force) {
      _uni.getLocation({
        type: "wgs84",
        success: function (res) {
          console.log("当前位置的经度：" + res.longitude);
          console.log("当前位置的纬度：" + res.latitude);
          let resStr = `${res.longitude},${res.latitude}`;
          _uni.setStorageSync(localStorageObj.location, resStr);
          resove(resStr);
        },
        fail: function () {
          reject();
        },
      });
    } else {
      resove(_uni.getStorageSync(localStorageObj.location));
    }
  });
};
/**
 * @description: 数组排序  array, key
 * @param {type}
 * @return:
 */
export const arrayOrder = (array, key) => {
  var len = array.length,
    temp;
  for (var i = 0; i < len - 1; i++) {
    for (var j = len - 1; j >= i; j--) {
      if (array[j] && array[j - 1] && array[j][key] < array[j - 1][key]) {
        temp = array[j];
        array[j] = array[j - 1];
        array[j - 1] = temp;
      }
    }
  }
  return array;
};

function urlEncode(param, key, encode) {
  if (param == null) return "";
  var paramStr = "";
  var t = typeof param;
  if (t == "string" || t == "number" || t == "boolean") {
    paramStr +=
      "&" +
      key +
      "=" +
      (encode == null || encode ? encodeURIComponent(param) : param);
  } else {
    for (var i in param) {
      var k =
        key == null
          ? i
          : key + (param instanceof Array ? "[" + i + "]" : "." + i);
      paramStr += urlEncode(param[i], k, encode);
    }
  }
  return paramStr;
}

// urlEncode
export const urlEncodeUrl = function (param, key, encode) {
  var s = urlEncode(param, key, encode);
  return s.slice(1);
};
export const isMobile = function (value) {
  //是否为手机号
  return /^(?:13\d|14\d|15\d|16\d|17\d|18\d|19\d)\d{5}(\d{3}|\*{3})$/.test(
    value
  );
};

export const getUserInfo = function (data) {
  return new Promise(async (resolve, reject) => {
    let res = await user_get(data).catch(() => {
      reject();
    });
    if (res && res.code === 200) {
      if (!data || !data.id) {
        store.commit("userInfo/setUserInfo", res.res);
      }
      resolve(res.res);
    } else {
      reject();
    }
  });
};

//打开授权设置（必须用户点击小程序才能打开授权设置，所以前面加了showModel）
export function openSetting() {
  // 打开小程序的设置
  // #ifdef MP
  uni.openSetting();
  // #endif
}
/**
 * @description: 消息订阅
 * @param {type}
 * @return:
 */
const showAlert = (reject) => {
  uni.showModal({
    confirmColor: "#1BB6B6",
    cancelColor: "#B4B9B9",
    title: "温馨提示",
    content: "请开启消息推送权限,方便及时接收您的约拍消息",
    confirmText: "去开启",
    success(res) {
      if (res.confirm) {
        //打开授权设置
        reject("");
        openSetting();
      } else {
        reject("您取消了授权");
      }
    },
  });
};
export const adminList = ["15910739636", "18500214323"];
export const sendMsg = (data, list, type) => {
  return new Promise((resove, reject) => {
    //   微信小程序
    // #ifdef MP-WEIXIN
    let msgids = [];
    if (data && data.length > 0) {
      for (let index = 0; index < data.length; index++) {
        const element1 = data[index];
        for (let index = 0; index < list.length; index++) {
          const element2 = list[index];
          if (element1.id === element2) {
            msgids.push(element1.value);
          }
        }
      }
    } else {
      resove(true);
      return;
    }
    console.log(msgids);
    wx.requestSubscribeMessage({
      tmplIds: msgids,
      success(res) {
        console.log(res, "++000");
        if (res.errMsg === "requestSubscribeMessage:ok") {
          let acc = true;
          for (let index = 0; index < msgids.length; index++) {
            const element = msgids[index];
            if (res[element] !== "accept") {
              acc = false;
            }
          }
          if (acc) {
            resove(res);
          } else {
            reject("请先勾选权限，方便及时接收您的约拍消息");
          }
        } else {
          showAlert(reject);
        }
      },
      fail(err) {
        console.log(err);
        showAlert(reject);
      },
    });
    // #endif
    // 非微信
    // #ifndef MP-WEIXIN
    resove(true);
    // #endif
  });
};
