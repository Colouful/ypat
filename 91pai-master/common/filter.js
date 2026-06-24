/*
 * @Author: shawn
 * @LastEditTime : 2020-01-04 22:33:46
 */
import { professMap, patstyleMap } from "@/common/dataMap";
// 收费方式
export const chargeway = type => {
  const typeList = ["希望互免", "我要收费", "可付费", "费用协商"];
  return typeList[type];
};

// 约拍对象
export const target = type => {
  const typeList = ["约摄影师", "约模特"];
  return typeList[type];
};

// 职业
export const profess = type => {
  const typeList = professMap;
  return typeList[type];
};
//  联系方式查看标识
export const linkwayflag = type => {
  const typeList = ["否", "是"];
  return typeList[type];
};
//   信用担保
export const creditflag = type => {
  const typeList = ["不需要", "需要"];
  return typeList[type];
};

// 实名认证
export const realnameflag = type => {
  const typeList = ["不需要", "需要"];
  return typeList[type];
};

// 收支类型
export const recordsType = type => {
  const typeList = [
    "充值",
    "好友邀请",
    "系统赠送",
    "发布约拍",
    "申请约拍",
    "查看联系方式"
  ];
  return typeList[type];
};

// 性别
export const gender = type => {
  const typeList = ["未知", "男", "女"];
  return typeList[type];
};

// 拍摄风格
export const patstyle = type => {
  const typeList = patstyleMap;
  return typeList[type];
};
