import { describe, expect, it, vi } from 'vitest'
import { readdirSync, readFileSync, statSync } from 'node:fs'

const serviceMock = vi.fn((config: unknown) => Promise.resolve(config))

vi.mock('@/api/request', () => ({
  default: serviceMock,
}))

describe('后台上传 API', () => {
  it('普通图片和水印作品图都通过后端代理上传', async () => {
    const { uploadFiles } = await import('@/api/modules/upload')
    const file = new File(['image'], 'image.jpg', { type: 'image/jpeg' })

    await uploadFiles([file])
    await uploadFiles([file], true)

    expect(serviceMock).toHaveBeenNthCalledWith(1, expect.objectContaining({
      method: 'POST',
      url: '/admin/upload',
      headers: { 'Content-Type': 'multipart/form-data' },
    }))
    expect(serviceMock).toHaveBeenNthCalledWith(2, expect.objectContaining({
      method: 'POST',
      url: '/admin/ypat/upload',
      headers: { 'Content-Type': 'multipart/form-data' },
    }))
  })

  it('不在管理后台源码中暴露 COS 密钥变量', () => {
    const srcRoot = `${process.cwd()}/src`
    const source = readSourceTree(srcRoot)

    expect(source).not.toMatch(/YPAT_COS|TENCENT_COS|COS_SECRET|SECRET_ID|SECRET_KEY/)
    expect(source).toContain('/admin/upload')
    expect(source).toContain('/admin/ypat/upload')
  })
})

function readSourceTree(dir: string): string {
  return readdirSync(dir)
    .filter((name) => !name.startsWith('.') && name !== '__tests__')
    .map((name) => {
      const path = `${dir}/${name}`
      if (statSync(path).isDirectory()) return readSourceTree(path)
      if (!/\.(ts|vue)$/.test(name)) return ''
      return readFileSync(path, 'utf8')
    })
    .join('\n')
}
