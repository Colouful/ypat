/**
 * 菜单配置（基于旧后台 nav.html 真实结构）
 *
 * 3 个系统对应旧后台 sysflag 1/2/3：
 * - 审核系统（sysflag=1）
 * - 查询系统（sysflag=2）
 * - 订单系统（sysflag=3）
 */

export interface MenuItem {
  /** 菜单标题 */
  title: string
  /** 路由路径 */
  path: string
  /** 组件路径（相对 views 目录，用于动态 import 映射） */
  component: string
  /** 图标名称（Element Plus Icons） */
  icon?: string
  /** 是否隐藏 */
  hidden?: boolean
}

export interface MenuGroup {
  /** 分组标题 */
  title: string
  /** 图标名称 */
  icon: string
  /** 子菜单 */
  children: MenuItem[]
}

export const menuConfig: MenuGroup[] = [
  {
    title: '审核系统',
    icon: 'Document',
    children: [
      { title: '申请列表', path: '/manage/ypat-list', component: 'manage/ypat-list/index' },
      { title: '实名列表', path: '/manage/user/index', component: 'manage/user-list/index' },
      { title: '产品列表', path: '/manage/product/index', component: 'manage/product-list/index' },
      { title: '作品管理', path: '/manage/work/index', component: 'manage/work-list/index' },
      { title: '投诉治理', path: '/manage/work-complain/index', component: 'manage/work-complain-list/index' },
      { title: '文章列表', path: '/article/index', component: 'article/list/index' },
      { title: '横幅列表', path: '/banner/index', component: 'banner/list/index' },
      { title: '后台代发约拍', path: '/ypat/edit', component: 'ypat/edit/index' },
    ],
  },
  {
    title: '查询系统',
    icon: 'Search',
    children: [
      { title: '用户列表', path: '/manage/query/index', component: 'query/user-list/index' },
      { title: '约拍列表', path: '/manage/query/ypat/appindex', component: 'query/ypat-list/index' },
      { title: '消息列表', path: '/manage/query/mess/messindex', component: 'query/mess-list/index' },
      { title: '公众号关注', path: '/pubevent/index', component: 'pubevent/list/index' },
    ],
  },
  {
    title: '订单系统',
    icon: 'ShoppingCart',
    children: [
      { title: '订单列表', path: '/manage/order/index', component: 'order/list/index' },
    ],
  },
  {
    title: '会员系统',
    icon: 'Medal',
    children: [
      { title: '套餐管理', path: '/member/plan', component: 'member/plan/index' },
      { title: '权益配置', path: '/member/rule', component: 'member/rule/index' },
      { title: '会员用户', path: '/member/user', component: 'member/user/index' },
      { title: '会员订单', path: '/member/order', component: 'member/order/index' },
      { title: '操作日志', path: '/member/log', component: 'member/log/index' },
    ],
  },
  {
    title: '内测数据',
    icon: 'DataAnalysis',
    children: [
      { title: '内测资源管理', path: '/internal-test/resource', component: 'internal-test/resource/index' },
      { title: '内测数据生成', path: '/internal-test/generator', component: 'internal-test/generator/index' },
    ],
  },
]
