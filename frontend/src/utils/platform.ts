// ============================================================
// YPAT Platform Utilities
// Cross-platform detection and storage abstraction
// ============================================================

export type PlatformType = 'mp-weixin' | 'h5' | 'app';

/**
 * 获取当前运行平台
 */
export function getPlatform(): PlatformType {
  // #ifdef MP-WEIXIN
  return 'mp-weixin';
  // #endif
  // #ifdef H5
  return 'h5';
  // #endif
  // #ifdef APP-PLUS
  return 'app';
  // #endif
}

/**
 * 是否为微信小程序环境
 */
export function isWeixin(): boolean {
  return getPlatform() === 'mp-weixin';
}

/**
 * 是否为H5环境
 */
export function isH5(): boolean {
  return getPlatform() === 'h5';
}

/**
 * 是否为App环境
 */
export function isApp(): boolean {
  return getPlatform() === 'app';
}

// ============================================================
// Storage Adapter - 封装 uni storage API
// ============================================================

/**
 * 设置存储数据
 * @param key 存储键名
 * @param value 存储值（自动序列化对象）
 */
export function setStorage<T = unknown>(key: string, value: T): void {
  try {
    const data = typeof value === 'string' ? value : JSON.stringify(value);
    uni.setStorageSync(key, data);
  } catch (e) {
    console.error(`[Storage] 写入失败 key=${key}`, e);
  }
}

/**
 * 获取存储数据
 * @param key 存储键名
 * @param defaultValue 默认值
 * @returns 存储值（自动反序列化）
 */
export function getStorage<T = unknown>(key: string, defaultValue?: T): T | undefined {
  try {
    const raw = uni.getStorageSync(key);
    if (!raw && raw !== 0 && raw !== false) return defaultValue;

    // 尝试解析JSON
    if (typeof raw === 'string') {
      try {
        return JSON.parse(raw) as T;
      } catch {
        return raw as unknown as T;
      }
    }
    return raw as T;
  } catch (e) {
    console.error(`[Storage] 读取失败 key=${key}`, e);
    return defaultValue;
  }
}

/**
 * 删除存储数据
 * @param key 存储键名
 */
export function removeStorage(key: string): void {
  try {
    uni.removeStorageSync(key);
  } catch (e) {
    console.error(`[Storage] 删除失败 key=${key}`, e);
  }
}
