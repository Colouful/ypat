/*
 * @Author: shawn
 * @LastEditTime: 2019-12-14 17:05:46
 */
/**
 * 微信小程序支付,仅支持微信支付(后续可能集成网页支付宝支付web-view)
 *
 * @param : provider(String) ->付款商家
 * @param : timeStamp(String) ->时间戳(当前支付时间)
 * @param : nonceStr(String) ->支付密匙
 * @param : packages(String) ->支付id
 * @param : signType(String) ->加密方式(默认MD5)
 * @param : paySign(String)
 *
 *
 *
 * 小程序支付调用
 *
 * wePay(provider, timeStamp, nonceStr, packages, signType, paySign,res=>{},fail=>{})
 */

const wePay = ({
  provider,
  timeStamp,
  nonceStr,
  packages,
  signType,
  paySign,
  success,
  fail
}) =>
  uni.requestPayment({
    provider,
    timeStamp,
    nonceStr,
    package: packages,
    signType,
    paySign,
    success(res) {
      success(res);
    },
    fail(err) {
      fail(err);
    }
  });

export default wePay;
