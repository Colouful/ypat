#!/usr/bin/env node
/**
 * YPAT 前端环境变量校验脚本
 *
 * 用法：
 *   node scripts/config/validate-frontend-env.mjs development
 *   node scripts/config/validate-frontend-env.mjs staging
 *   node scripts/config/validate-frontend-env.mjs production
 *
 * 不依赖 Vite，能在 CI 中直接运行。
 */

import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const FRONTEND_DIR = path.resolve(__dirname, '../../frontend')

const ALLOWED_ENVS = ['development', 'staging', 'production']
const FORBIDDEN_PRODUCTION_HOSTS = ['panghu.work', 'www.panghu.work', 'localhost', '127.0.0.1']
const PLACEHOLDER_VALUES = new Set(['', '<placeholder>', 'CHANGE_ME', 'TODO', 'example.invalid'])

function loadEnvFile(env) {
  const filePath = path.join(FRONTEND_DIR, `.env.${env}`)
  if (!fs.existsSync(filePath)) {
    console.error(`✗ 环境文件不存在: ${filePath}`)
    console.error(`  请基于 frontend/.env.${env}.example 创建。`)
    return null
  }
  const content = fs.readFileSync(filePath, 'utf8')
  const vars = {}
  for (const line of content.split('\n')) {
    const trimmed = line.trim()
    if (!trimmed || trimmed.startsWith('#')) continue
    const eq = trimmed.indexOf('=')
    if (eq <= 0) continue
    const key = trimmed.slice(0, eq).trim()
    let value = trimmed.slice(eq + 1).trim()
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1)
    }
    vars[key] = value
  }
  return vars
}

function isIPv4(s) {
  return /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/.test(s)
}

function validateUrl(value, { requireHttps, allowLocalhost, allowIp }) {
  if (PLACEHOLDER_VALUES.has(value)) {
    throw new Error('地址是占位符，未注入真实值')
  }
  let url
  try {
    url = new URL(value)
  } catch {
    throw new Error(`不是合法 URL: ${value}`)
  }
  if (requireHttps && url.protocol !== 'https:') {
    throw new Error(`必须使用 HTTPS 协议，当前: ${url.protocol}`)
  }
  if (!allowLocalhost && url.hostname === 'localhost') {
    throw new Error(`不允许使用 localhost`)
  }
  if (!allowIp && isIPv4(url.hostname)) {
    throw new Error(`不允许使用裸 IP 地址: ${url.hostname}`)
  }
  if (url.port && url.port !== '443' && url.port !== '80') {
    // 显式非标准端口（443/80 视为标准）
    // 注意：development 允许任意端口，因为本地 dev server 通常用 3000/8088 等
    if (!allowLocalhost) {
      throw new Error(`不允许使用非标准端口: ${url.port}`)
    }
  }
  return url
}

function validate(envName) {
  const errors = []
  const vars = loadEnvFile(envName)
  if (!vars) {
    return { ok: false, errors: ['环境文件读取失败'] }
  }

  // 公共规则
  if (vars.VITE_APP_ENV !== envName) {
    errors.push(`VITE_APP_ENV 与目标环境不一致：当前='${vars.VITE_APP_ENV}'，期望='${envName}'`)
  }

  const api = vars.VITE_API_BASE_URL
  const img = vars.VITE_IMAGE_BASE_URL

  if (envName === 'development') {
    // 允许 localhost 和 HTTP
    if (!api) errors.push('VITE_API_BASE_URL 不能为空')
    else
      try {
        validateUrl(api, { requireHttps: false, allowLocalhost: true, allowIp: true })
      } catch (e) {
        errors.push(`VITE_API_BASE_URL: ${e.message}`)
      }
    if (!img) errors.push('VITE_IMAGE_BASE_URL 不能为空')
    else
      try {
        validateUrl(img, { requireHttps: false, allowLocalhost: true, allowIp: true })
      } catch (e) {
        errors.push(`VITE_IMAGE_BASE_URL: ${e.message}`)
      }
  } else if (envName === 'staging') {
    // 必须 HTTPS，且不允许 staging.example.invalid 类占位符
    if (!api || PLACEHOLDER_VALUES.has(api)) errors.push('VITE_API_BASE_URL 必须注入真实 staging 地址')
    else
      try {
        const u = validateUrl(api, { requireHttps: true, allowLocalhost: false, allowIp: false })
        if (FORBIDDEN_PRODUCTION_HOSTS.includes(u.hostname)) {
          // staging 允许使用 panghu.work
        }
      } catch (e) {
        errors.push(`VITE_API_BASE_URL: ${e.message}`)
      }
    if (!img || PLACEHOLDER_VALUES.has(img)) errors.push('VITE_IMAGE_BASE_URL 必须注入真实 staging 地址')
    else
      try {
        validateUrl(img, { requireHttps: true, allowLocalhost: false, allowIp: false })
      } catch (e) {
        errors.push(`VITE_IMAGE_BASE_URL: ${e.message}`)
      }
  } else if (envName === 'production') {
    if (!api || PLACEHOLDER_VALUES.has(api))
      errors.push('VITE_API_BASE_URL 必须注入真实 production 地址（不得使用 CHANGE_ME/<placeholder>/空值）')
    else
      try {
        const u = validateUrl(api, { requireHttps: true, allowLocalhost: false, allowIp: false })
        if (FORBIDDEN_PRODUCTION_HOSTS.includes(u.hostname)) {
          errors.push(
            `VITE_API_BASE_URL 使用禁止的 production 主机名 '${u.hostname}'：production 必须有独立域名，不能是 staging/localhost/IP`,
          )
        }
      } catch (e) {
        errors.push(`VITE_API_BASE_URL: ${e.message}`)
      }
    if (!img || PLACEHOLDER_VALUES.has(img))
      errors.push('VITE_IMAGE_BASE_URL 必须注入真实 production 地址')
    else
      try {
        const u = validateUrl(img, { requireHttps: true, allowLocalhost: false, allowIp: false })
        if (FORBIDDEN_PRODUCTION_HOSTS.includes(u.hostname)) {
          errors.push(`VITE_IMAGE_BASE_URL 使用禁止的 production 主机名 '${u.hostname}'`)
        }
      } catch (e) {
        errors.push(`VITE_IMAGE_BASE_URL: ${e.message}`)
      }
  }

  return { ok: errors.length === 0, errors }
}

function main() {
  const env = process.argv[2]
  if (!env || !ALLOWED_ENVS.includes(env)) {
    console.error(`✗ 用法: ${path.basename(process.argv[1])} <${ALLOWED_ENVS.join('|')}>`)
    process.exit(2)
  }

  const result = validate(env)
  if (result.ok) {
    console.log(`✓ frontend environment '${env}' passed validation`)
    process.exit(0)
  } else {
    console.error(`✗ frontend environment '${env}' failed validation:`)
    for (const e of result.errors) {
      console.error(`  - ${e}`)
    }
    process.exit(1)
  }
}

main()