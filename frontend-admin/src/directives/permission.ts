import type { Directive, DirectiveBinding } from 'vue'

/**
 * v-permission 按钮权限指令
 *
 * 用法：
 *   v-permission="'user:add'"
 *   v-permission="['user:add', 'user:edit']"
 *
 * 旧后台无细粒度权限，当前所有登录用户均有权限。
 * 预留指令，后续可通过 auth store 扩展权限判断。
 */
export const permission: Directive = {
  mounted(_el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(binding)
  },
  updated(_el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(binding)
  },
}

function checkPermission(binding: DirectiveBinding): void {
  const { value } = binding

  if (!value) {
    return
  }

  // 旧后台无细粒度权限，当前所有登录用户均有权限
  // 预留扩展点：后续可从 auth store 获取权限列表进行判断
  // const authStore = useAuthStore()
  // const permissions = authStore.permissions
  // if (Array.isArray(value)) {
  //   if (value.length === 0) return
  //   const hasPermission = value.some((p: string) => permissions.includes(p))
  //   if (!hasPermission) {
  //     el.parentNode?.removeChild(el)
  //   }
  // } else {
  //   if (!permissions.includes(value as string)) {
  //     el.parentNode?.removeChild(el)
  //   }
  // }
}
