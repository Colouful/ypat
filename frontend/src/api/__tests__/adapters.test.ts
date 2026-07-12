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
    vi.stubEnv('VITE_IMAGE_BASE_URL', 'https://fastdfs.panghu.work/')

    const { normalizeImageUrl } = await loadAdapters()

    expect(normalizeImageUrl('group1/M00/00/00/a.jpg')).toBe('https://fastdfs.panghu.work/group1/M00/00/00/a.jpg')
    expect(normalizeImageUrl('/group1/M00/00/00/a.jpg')).toBe('https://fastdfs.panghu.work/group1/M00/00/00/a.jpg')
  })

  it('keeps absolute image URLs unchanged', async () => {
    const { normalizeImageUrl } = await loadAdapters()

    expect(normalizeImageUrl('https://cdn.example.test/a.jpg')).toBe('https://cdn.example.test/a.jpg')
  })

  it('rewrites legacy local FastDFS URLs to the configured image base URL', async () => {
    vi.stubEnv('VITE_APP_ENV', 'development')
    vi.stubEnv('VITE_IMAGE_BASE_URL', 'https://fastdfs.panghu.work/')

    const { normalizeImageUrl } = await loadAdapters()

    expect(normalizeImageUrl('http://localhost:8888group1/M00/00/00/a.jpg')).toBe(
      'https://fastdfs.panghu.work/group1/M00/00/00/a.jpg',
    )
    expect(normalizeImageUrl('http://localhost:8888/group1/M00/00/00/a.jpg')).toBe(
      'https://fastdfs.panghu.work/group1/M00/00/00/a.jpg',
    )
  })

  it('rewrites legacy panghu files URLs to the configured image base URL', async () => {
    vi.stubEnv('VITE_APP_ENV', 'development')
    vi.stubEnv('VITE_IMAGE_BASE_URL', 'https://fastdfs.panghu.work/')

    const { normalizeImageUrl } = await loadAdapters()

    expect(normalizeImageUrl('https://panghu.work/files/group1/M00/00/00/a.jpg')).toBe(
      'https://fastdfs.panghu.work/group1/M00/00/00/a.jpg',
    )
  })
})

describe('normalizePageResult', () => {
  it('normalizes legacy content pages and totals with caller pagination fallbacks', async () => {
    const { normalizePageResult } = await loadAdapters()

    expect(normalizePageResult(
      { content: [{ id: 5 }], pages: 3, totals: 21 },
      { number: 1, size: 10 },
    )).toEqual({
      content: [{ id: 5 }],
      totalElements: 21,
      totalPages: 3,
      number: 1,
      size: 10,
    })
  })
})
