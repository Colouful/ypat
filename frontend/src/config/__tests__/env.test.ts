import { describe, it, expect, afterEach, vi } from 'vitest'

const originalEnv = { ...import.meta.env }

function setEnv(values: Record<string, string>) {
  for (const [k, v] of Object.entries(values)) {
    ;(import.meta.env as Record<string, string>)[k] = v
  }
}

function restoreEnv() {
  Object.keys(import.meta.env).forEach((key) => {
    if (key.startsWith('VITE_')) {
      delete (import.meta.env as Record<string, string | undefined>)[key]
    }
  })
  Object.entries(originalEnv).forEach(([key, value]) => {
    if (key.startsWith('VITE_')) {
      ;(import.meta.env as Record<string, string>)[key] = value as string
    }
  })
}

async function loadEnvConfig() {
  // envConfig 是模块加载期求值的常量，必须 reset 后重新 import 才能拿到针对当前 env 计算的新值
  vi.resetModules()
  const mod = await import('../../config/env')
  return mod.envConfig
}

async function expectThrowsOnLoad(matcher: string | RegExp) {
  vi.resetModules()
  await expect(() => import('../../config/env')).rejects.toThrow(matcher)
}

describe('envConfig', () => {
  afterEach(() => {
    restoreEnv()
  })

  describe('development 环境', () => {
    it('应允许 HTTP localhost', async () => {
      setEnv({ VITE_APP_ENV: 'development' })
      delete (import.meta.env as Record<string, string | undefined>).VITE_API_BASE_URL
      const result = await loadEnvConfig()
      expect(result.env).toBe('development')
      expect(result.apiBaseUrl).toBe('http://localhost:8080/api')
      expect(result.imageBaseUrl).toBe('http://localhost:8080/files')
    })
  })

  describe('staging 环境', () => {
    it('应拒绝 HTTP URL', async () => {
      setEnv({
        VITE_APP_ENV: 'staging',
        VITE_API_BASE_URL: 'http://panghu.work/api',
        VITE_IMAGE_BASE_URL: 'http://panghu.work/files',
      })
      await expectThrowsOnLoad(/HTTPS/)
    })

    it('应拒绝 localhost', async () => {
      setEnv({
        VITE_APP_ENV: 'staging',
        VITE_API_BASE_URL: 'https://localhost/api',
        VITE_IMAGE_BASE_URL: 'https://localhost/files',
      })
      await expectThrowsOnLoad(/localhost/)
    })

    it('应拒绝 IP 地址', async () => {
      setEnv({
        VITE_APP_ENV: 'staging',
        VITE_API_BASE_URL: 'https://82.156.14.216/api',
        VITE_IMAGE_BASE_URL: 'https://82.156.14.216/files',
      })
      await expectThrowsOnLoad(/IP/)
    })

    it('应允许 HTTPS 域名', async () => {
      setEnv({
        VITE_APP_ENV: 'staging',
        VITE_API_BASE_URL: 'https://panghu.work/api',
        VITE_IMAGE_BASE_URL: 'https://panghu.work/files',
      })
      const result = await loadEnvConfig()
      expect(result.env).toBe('staging')
      expect(result.apiBaseUrl).toBe('https://panghu.work/api')
      expect(result.imageBaseUrl).toBe('https://panghu.work/files')
    })

    it('应拒绝空 URL', async () => {
      setEnv({
        VITE_APP_ENV: 'staging',
        VITE_API_BASE_URL: '',
        VITE_IMAGE_BASE_URL: '',
      })
      await expectThrowsOnLoad(/必须配置/)
    })

    it('应拒绝非标准端口', async () => {
      setEnv({
        VITE_APP_ENV: 'staging',
        VITE_API_BASE_URL: 'https://panghu.work:8443/api',
        VITE_IMAGE_BASE_URL: 'https://panghu.work:8443/files',
      })
      await expectThrowsOnLoad(/non-standard port/)
    })
  })

  describe('production 环境', () => {
    it('应拒绝 HTTP URL', async () => {
      setEnv({
        VITE_APP_ENV: 'production',
        VITE_API_BASE_URL: 'http://example.com/api',
        VITE_IMAGE_BASE_URL: 'http://example.com/files',
      })
      await expectThrowsOnLoad(/HTTPS/)
    })

    it('应拒绝 IP 地址', async () => {
      setEnv({
        VITE_APP_ENV: 'production',
        VITE_API_BASE_URL: 'https://1.2.3.4/api',
        VITE_IMAGE_BASE_URL: 'https://1.2.3.4/files',
      })
      await expectThrowsOnLoad(/IP/)
    })

    it('应允许 HTTPS 域名', async () => {
      setEnv({
        VITE_APP_ENV: 'production',
        VITE_API_BASE_URL: 'https://api.production-ci.example.invalid/api',
        VITE_IMAGE_BASE_URL: 'https://api.production-ci.example.invalid/files',
      })
      const result = await loadEnvConfig()
      expect(result.env).toBe('production')
      expect(result.apiBaseUrl).toBe('https://api.production-ci.example.invalid/api')
    })
  })
})
