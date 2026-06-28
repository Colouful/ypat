import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { envConfig } from '../../config/env'

describe('envConfig', () => {
  const originalEnv = { ...import.meta.env }

  afterEach(() => {
    // 恢复原始环境变量
    Object.keys(import.meta.env).forEach((key) => {
      if (key.startsWith('VITE_')) {
        delete (import.meta.env as any)[key]
      }
    })
    Object.entries(originalEnv).forEach(([key, value]) => {
      if (key.startsWith('VITE_')) {
        ;(import.meta.env as any)[key] = value
      }
    })
  })

  describe('development 环境', () => {
    it('应允许 HTTP localhost', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'development'
      // 不设置 VITE_API_BASE_URL，使用默认 http://localhost:8088
      const result = envConfig
      expect(result.env).toBe('development')
      expect(result.apiBaseUrl).toBe('http://localhost:8088')
    })
  })

  describe('staging 环境', () => {
    it('应拒绝 HTTP URL', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'staging'
      ;(import.meta.env as any).VITE_API_BASE_URL = 'http://panghu.work/api'
      ;(import.meta.env as any).VITE_IMAGE_BASE_URL = 'http://panghu.work/files'
      expect(() => envConfig).toThrow('HTTPS')
    })

    it('应拒绝 localhost', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'staging'
      ;(import.meta.env as any).VITE_API_BASE_URL = 'https://localhost/api'
      ;(import.meta.env as any).VITE_IMAGE_BASE_URL = 'https://localhost/files'
      expect(() => envConfig).toThrow('localhost')
    })

    it('应拒绝 IP 地址', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'staging'
      ;(import.meta.env as any).VITE_API_BASE_URL = 'https://82.156.14.216/api'
      ;(import.meta.env as any).VITE_IMAGE_BASE_URL = 'https://82.156.14.216/files'
      expect(() => envConfig).toThrow('IP')
    })

    it('应允许 HTTPS 域名', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'staging'
      ;(import.meta.env as any).VITE_API_BASE_URL = 'https://panghu.work/api'
      ;(import.meta.env as any).VITE_IMAGE_BASE_URL = 'https://panghu.work/files'
      const result = envConfig
      expect(result.env).toBe('staging')
      expect(result.apiBaseUrl).toBe('https://panghu.work/api')
      expect(result.imageBaseUrl).toBe('https://panghu.work/files')
    })

    it('应拒绝空 URL', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'staging'
      ;(import.meta.env as any).VITE_API_BASE_URL = ''
      ;(import.meta.env as any).VITE_IMAGE_BASE_URL = ''
      expect(() => envConfig).toThrow('必须配置')
    })

    it('应拒绝非标准端口', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'staging'
      ;(import.meta.env as any).VITE_API_BASE_URL = 'https://panghu.work:8443/api'
      ;(import.meta.env as any).VITE_IMAGE_BASE_URL = 'https://panghu.work:8443/files'
      expect(() => envConfig).toThrow('non-standard port')
    })
  })

  describe('production 环境', () => {
    it('应拒绝 HTTP URL', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'production'
      ;(import.meta.env as any).VITE_API_BASE_URL = 'http://example.com/api'
      ;(import.meta.env as any).VITE_IMAGE_BASE_URL = 'http://example.com/files'
      expect(() => envConfig).toThrow('HTTPS')
    })

    it('应拒绝 IP 地址', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'production'
      ;(import.meta.env as any).VITE_API_BASE_URL = 'https://1.2.3.4/api'
      ;(import.meta.env as any).VITE_IMAGE_BASE_URL = 'https://1.2.3.4/files'
      expect(() => envConfig).toThrow('IP')
    })

    it('应允许 HTTPS 域名', () => {
      ;(import.meta.env as any).VITE_APP_ENV = 'production'
      ;(import.meta.env as any).VITE_API_BASE_URL = 'https://www.panghu.work/api'
      ;(import.meta.env as any).VITE_IMAGE_BASE_URL = 'https://www.panghu.work/files'
      const result = envConfig
      expect(result.env).toBe('production')
      expect(result.apiBaseUrl).toBe('https://www.panghu.work/api')
    })
  })
})