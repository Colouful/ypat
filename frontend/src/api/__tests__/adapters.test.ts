import { afterEach, describe, expect, it, vi } from 'vitest'

async function loadAdapters() {
  vi.resetModules()
  return import('../adapters')
}

describe('normalizeImageUrl', () => {
  afterEach(() => {
    vi.unstubAllEnvs()
  })

  it('joins image base URL and FastDFS path with a slash', async () => {
    vi.stubEnv('VITE_APP_ENV', 'staging')
    vi.stubEnv('VITE_API_BASE_URL', 'https://panghu.work/api')
    vi.stubEnv('VITE_IMAGE_BASE_URL', 'https://panghu.work/files')

    const { normalizeImageUrl } = await loadAdapters()

    expect(normalizeImageUrl('group1/M00/00/00/a.jpg')).toBe('https://panghu.work/files/group1/M00/00/00/a.jpg')
    expect(normalizeImageUrl('/group1/M00/00/00/a.jpg')).toBe('https://panghu.work/files/group1/M00/00/00/a.jpg')
  })

  it('keeps absolute image URLs unchanged', async () => {
    const { normalizeImageUrl } = await loadAdapters()

    expect(normalizeImageUrl('https://cdn.example.test/a.jpg')).toBe('https://cdn.example.test/a.jpg')
  })
})
