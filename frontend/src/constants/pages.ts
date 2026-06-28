// ============================================================
// YPAT Page Path Constants
// Organized by module for easy reference and navigation
// ============================================================

/** 首页模块 */
export const HOME = {
  INDEX: '/pages/home/index',
  SEARCH: '/pages/home/search',
  DETAIL: '/pages/home/detail',
} as const;

/** 登录模块 */
export const LOGIN = {
  INDEX: '/pages/login/index',
  PHONE: '/pages/login/phone',
  REGISTER: '/pages/login/register',
} as const;

/** 约拍模块 */
export const YPAT = {
  LIST: '/pages/ypat/list',
  DETAIL: '/pages/ypat/detail',
  PUBLISH: '/pages/ypat/publish',
  APPLY: '/pages/ypat/apply',
  MY_PUBLISHED: '/pages/ypat/my-published',
  MY_APPLIED: '/pages/ypat/my-applied',
} as const;

/** 我的模块 */
export const MINE = {
  INDEX: '/pages/mine/index',
  PROFILE: '/pages/mine/profile',
  EDIT_PROFILE: '/pages/mine/edit-profile',
  SETTINGS: '/pages/mine/settings',
  WALLET: '/pages/mine/wallet',
  TOPUP: '/pages/mine/topup',
  RECORDS: '/pages/mine/records',
  REALNAME: '/pages/mine/realname',
  FEEDBACK: '/pages/mine/feedback',
  ABOUT: '/pages/mine/about',
} as const;

/** 消息模块 */
export const MESSAGE = {
  INDEX: '/pages/message/index',
  CHAT: '/pages/message/chat',
  NOTIFICATION: '/pages/message/notification',
  SYSTEM: '/pages/message/system',
} as const;

/** 订单模块 */
export const ORDER = {
  LIST: '/pages/order/list',
  DETAIL: '/pages/order/detail',
  PAY_RESULT: '/pages/order/pay-result',
} as const;

/** 通用页面 */
export const COMMON = {
  WEBVIEW: '/pages/common/webview',
  PREVIEW_IMAGE: '/pages/common/preview-image',
} as const;

/** 所有页面路径集合 */
export const PAGES = {
  HOME,
  LOGIN,
  YPAT,
  MINE,
  MESSAGE,
  ORDER,
  COMMON,
} as const;

/** 需要登录才能访问的页面列表 */
export const PROTECTED_PAGES: string[] = [
  YPAT.PUBLISH,
  YPAT.APPLY,
  YPAT.MY_PUBLISHED,
  YPAT.MY_APPLIED,
  MINE.INDEX,
  MINE.PROFILE,
  MINE.EDIT_PROFILE,
  MINE.SETTINGS,
  MINE.WALLET,
  MINE.TOPUP,
  MINE.RECORDS,
  MINE.REALNAME,
  MINE.FEEDBACK,
  MESSAGE.INDEX,
  MESSAGE.CHAT,
  MESSAGE.NOTIFICATION,
  MESSAGE.SYSTEM,
  ORDER.LIST,
  ORDER.DETAIL,
];

/**
 * 判断页面是否需要登录
 * @param path 页面路径
 */
export function isProtectedPage(path: string): boolean {
  return PROTECTED_PAGES.some((page) => path.startsWith(page));
}
