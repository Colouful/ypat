/*
 * @Author: shawn
 * @LastEditTime: 2019-10-20 15:23:11
 */
//公共js,以及基本方法封装 nvue里使用
const fetch = {
  interfaceUrl: function() {
    //接口地址
    return "";
  },
  toast: function(tips) {
    uni.showToast({
      title: tips || "出错啦~",
      icon: "none",
      duration: 2000
    });
  },
  request: function({ url, postData, method, type, showLoading }) {
    //接口请求
    if (showLoading) {
      uni.showLoading({
        mask: true,
        title: "请稍候..."
      });
    }
    // const token = postData.token || "";
    // delete postData["token"];
    const params = {
    //   data: method === "POST" ? postData : JSON.stringify(postData)
    };
    return new Promise((resolve, reject) => {
      uni.request({
        url: Vue.$Url + url,
        data: method === "POST" ? JSON.stringify(params) : params,
        header: {
          "content-type": type
            ? "application/x-www-form-urlencoded"
            : "application/json",
        //   authorization: '111',
          security: "1"
        },
        method: method, //'GET','POST'
        dataType: "json",
        success: res => {
          showLoading && uni.hideLoading();
          resolve(res.data);
        },
        fail: res => {
          fetch.toast("网络不给力，请稍后再试~");
          reject(res);
        }
      });
    });
  }
};

module.exports = {
  request: fetch.request,
  toast: fetch.toast
};
