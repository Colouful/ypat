const storage = new Map<string, unknown>()

const uniStub = {
  getStorageSync(key: string): unknown {
    return storage.get(key)
  },
  setStorageSync(key: string, value: unknown): void {
    storage.set(key, value)
  },
  removeStorageSync(key: string): void {
    storage.delete(key)
  },
  clearStorageSync(): void {
    storage.clear()
  },
}

Reflect.set(globalThis, 'uni', uniStub)

beforeEach(() => {
  storage.clear()
})
