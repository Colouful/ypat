/*
 * @Author: shawn
 * @LastEditTime: 2020-05-18 22:32:02
 */
import http from "./interface";
import { getUrl } from "../utils";
/**
 * 将业务所有接口统一起来便于维护
 * 如果项目很大可以将 url 独立成文件，接口分成不同的模块
 *
 */

// // 单独导出(测试接口) import {test} from '@/common/vmeitime-http/'
// export const test = data => {
//   /* http.config.baseUrl = "http://localhost:8080/api/"
// 	//设置请求前拦截器
// 	http.interceptor.request = (config) => {
// 		config.header = {
// 			"token": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
// 		}
// 	} */
//   //设置请求结束后拦截器
//   http.interceptor.response = response => {
//     console.log("个性化response....");
//     //判断返回状态 执行相应操作
//     return response;
//   };
//   return http.request({
//     baseUrl: "https://unidemo.dcloud.net.cn/",
//     url: "ajax/echo/text?name=uni-app",
//     dataType: "text",
//     data
//   });
// };

// 首页 推荐
export const ypat_tc_list = (data) => {
  const url = getUrl("/ypat/tc/list", data);
  return http.request({
    url,
    method: "GET",
  });
};

// 我的-收到约拍消息总数
export const my_ypat_rec_count = () => {
  const url = getUrl("/my/ypat/rec/count");
  return http.request({
    url,
    method: "GET",
  });
};

// 我的-收到未读
export const my_ypat_rec_unread_count = () => {
  const url = getUrl("/my/ypat/rec/unread/count");
  return http.request({
    url,
    method: "GET",
  });
};

// 我的-申请未读
export const my_ypat_send_unread_count = () => {
  const url = getUrl("/my/ypat/send/unread/count");
  return http.request({
    url,
    method: "GET",
  });
};

// 我的-发送约拍消息列表
export const my_ypat_send_list = (data) => {
  const url = getUrl("/my/ypat/send/list");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 我的-收到约拍消息列表
export const my_ypat_rec_list = (data) => {
  const url = getUrl("/my/ypat/rec/list", data);
  return http.request({
    url,
    method: "GET",
  });
};

// 获取邀请好友列表
export const my_frd_list = (data) => {
  const url = getUrl("/my/frd/list", data);
  return http.request({
    url,
    method: "GET",
  });
};

// 申请约拍列表
export const my_ypat_app_list = (data) => {
  const url = getUrl("/my/ypat/app/list", data);
  return http.request({
    url,
    method: "GET",
  });
};

// 我的-发布约拍列表
export const my_ypat_pub_list = (data) => {
  const url = getUrl("/my/ypat/pub/list", data);
  return http.request({
    url,
    method: "GET",
  });
};

// 我的-拍拍豆收支列表
export const my_ppd_list = (data) => {
  const url = getUrl("/my/ppd/list", data);
  return http.request({
    url,
    method: "GET",
  });
};

// 获取产品列表
export const product_list = (data) => {
  const url = getUrl("/product/list", data);
  return http.request({
    url,
    method: "GET",
  });
};

// 获取用户详情
export const user_get = (data) => {
  const url = getUrl("/user/get", data);
  return http.request({
    url,
    method: "GET",
  });
};

// 授权码code换取openid
export const user_code = (data) => {
  const url = getUrl("/user/code", data);
  return http.request({
    url,
    method: "GET",
  });
};
// 授权码code换取openid
export const bd_code = (data) => {
  const url = getUrl("/bd/code", data);
  return http.request({
    url,
    method: "POST",
  });
};

// 登录接口（微信授权成功后回调该接口，完成登录）
export const user_login = (data) => {
  const url = getUrl("/user/login");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 登录接口（百度授权成功后回调该接口，完成登录）
export const bd_login = (data) => {
  const url = getUrl("/bd/login");
  return http.request({
    url,
    method: "POST",
    data,
  });
};
// 我的-收藏约拍列表
export const my_ypat_sc_list = (data) => {
  const url = getUrl("/my/ypat/sc/list", data);
  return http.request({
    url,
    method: "GET",
  });
};
// 阅读+1
export const ypat_yd_add = (data) => {
  const url = getUrl("/ypat/yd/add");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 约拍详情
export const ypat_get = (data) => {
  const url = getUrl("/ypat/get");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 发布约拍
export const ypat_submit = (data) => {
  const url = getUrl("/ypat/submit");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 收藏约拍
export const my_ypat_sc_add = (data) => {
  const url = getUrl("/my/ypat/sc/add");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 查看联系方式
export const user_linkway_get = (data) => {
  const url = getUrl("/user/linkway/get");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 查看消息详情
export const mess_get = (data) => {
  const url = getUrl("/mess/get");
  return http.request({
    url,
    method: "GET",
    data,
  });
};
// 实名信息保存
export const oauth_add = (data) => {
  const url = getUrl("/oauth/add");
  return http.request({
    url,
    method: "POST",
    data,
  });
};
// 订单创建
export const order_create = (data) => {
  const url = getUrl("/order/create");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 更新用户信息（类型长度参照用户表）
export const user_upd = (data) => {
  const url = getUrl("/user/upd");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 获取实名审核结果
export const oauth_get = (data) => {
  const url = getUrl("/oauth/get");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 收到约拍、请求约拍
export const my_ypat_rec_add = (data) => {
  const url = getUrl("/my/ypat/rec/add");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 约拍列表（推荐
export const ypat_tj_list = (data) => {
  const url = getUrl("/ypat/tj/list");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 首页最新
export const ypat_zx_list = (data) => {
  const url = getUrl("/ypat/zx/list");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 10-4总未读 接口
export const my_ypat_unread_count = (data) => {
  const url = getUrl("/my/ypat/unread/count");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 获取约拍人头像列表
export const my_ypat_head_list = (data) => {
  const url = getUrl("/my/ypat/head/list");
  return http.request({
    url,
    method: "GET",
    data,
  });
};
// ocr识别
export const oauth_ocr = (data) => {
  const url = getUrl("/oauth/ocr");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 获取消息模板id
export const tmplid_list = (data) => {
  const url = getUrl("/tmplid/list");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 获取地址
export const area_list = (data) => {
  const url = getUrl("/area/list");
  return http.request({
    url,
    method: "GET",
    data,
  });
};
// 获取地址
export const area_list_a = (data) => {
  const url = getUrl("/area/list/a");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 发布审核
export const ypat_audit = (data) => {
  const url = getUrl("/ypat/audit");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 实名审核
export const oauth_audit = (data) => {
  const url = getUrl("/oauth/audit");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 消息审核
export const ypat_audit_list = (data) => {
  const url = getUrl("/ypat/audit/list");
  return http.request({
    url,
    method: "GET",
    data,
  });
};

// 是否展示实名
export const param_list = (data) => {
  const url = getUrl("/param/list");
  return http.request({
    url,
    method: "GET",
    data,
  });
};
// 推荐或取消推荐接口

// 参数： id=，recomflag=0或1 （0：不推荐，1：人工推荐）
export const ypat_upRecom = (data) => {
  const url = getUrl("/ypat/upRecom");
  return http.request({
    url,
    method: "POST",
    data,
  });
};

// 二维码生成
export const qr_code = (data) => {
  const url = getUrl("/qr/code");
  return http.request({
    url,
    method: "GET",
    data,
  });
};
