import { beforeEach, describe, expect, it, vi } from 'vitest'

const uploadMock = vi.hoisted(() => vi.fn())

vi.mock('../request-adapter', () => ({
  upload: uploadMock,
}))

vi.mock('@/config/env', () => ({
  envConfig: {
    imageUploadApiBaseUrl: 'https://panghu.work/api',
  },
}))

import { uploadImage, uploadVideo } from '../modules/media'

describe('media upload API', () => {
  beforeEach(() => {
    uploadMock.mockReset()
    uploadMock.mockResolvedValue({
      success: true,
      data: { id: 7, url: 'https://cdn.example.test/a.jpg', type: 'IMAGE', fileSize: 12 },
      code: '200',
      message: '',
    })
  })

  it('uploads images through the staging upload host while preserving shared token handling', async () => {
    await expect(uploadImage('/tmp/a.jpg')).resolves.toEqual({
      id: 7,
      url: 'https://cdn.example.test/a.jpg',
      type: 'IMAGE',
      fileSize: 12,
    })

    expect(uploadMock).toHaveBeenCalledWith({
      url: 'https://panghu.work/api/work/upload/image',
      filePath: '/tmp/a.jpg',
      name: 'file',
      showLoading: false,
    })
  })

  it('uploads videos through the shared request adapter base URL and token handling', async () => {
    uploadMock.mockResolvedValueOnce({
      success: true,
      data: { id: 8, url: 'https://cdn.example.test/a.mp4', type: 'VIDEO', fileSize: 24 },
      code: '200',
      message: '',
    })

    await expect(uploadVideo('/tmp/a.mp4')).resolves.toMatchObject({
      id: 8,
      url: 'https://cdn.example.test/a.mp4',
      type: 'VIDEO',
    })

    expect(uploadMock).toHaveBeenCalledWith({
      url: '/work/upload/video',
      filePath: '/tmp/a.mp4',
      name: 'file',
      showLoading: false,
    })
  })
})
