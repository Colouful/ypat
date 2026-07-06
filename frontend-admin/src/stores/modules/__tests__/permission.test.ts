import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { usePermissionStore } from '../permission'

describe('permission dynamic routes', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('maps member menu entries to real pages instead of placeholders', () => {
    const store = usePermissionStore()
    const routes = store.generateRoutes()
    const memberPaths = ['member/plan', 'member/rule', 'member/user', 'member/order', 'member/log']

    for (const path of memberPaths) {
      const route = routes.find((item) => item.path === path)

      expect(route, `${path} route should exist`).toBeTruthy()
      expect(route?.meta?.placeholder, `${path} should not use PagePlaceholder`).toBeUndefined()
    }
  })
})
