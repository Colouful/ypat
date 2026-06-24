function removeDataUrlHeader(value: string): string {
  const index = value.indexOf(',')
  return value.startsWith('data:') && index >= 0 ? value.slice(index + 1) : value
}

function blobToBase64(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(removeDataUrlHeader(String(reader.result || '')))
    reader.onerror = () => reject(new Error('读取图片失败'))
    reader.readAsDataURL(blob)
  })
}

export async function filePathToBase64(filePath: string): Promise<string> {
  if (!filePath) throw new Error('图片路径不能为空')

  // #ifdef H5
  const response = await fetch(filePath)
  if (!response.ok) throw new Error('读取图片失败')
  return blobToBase64(await response.blob())
  // #endif

  // #ifndef H5
  return new Promise((resolve, reject) => {
    uni.getFileSystemManager().readFile({
      filePath,
      encoding: 'base64',
      success: (result) => {
        const value = String(result.data || '')
        if (!value) {
          reject(new Error('图片内容为空'))
          return
        }
        resolve(removeDataUrlHeader(value))
      },
      fail: () => reject(new Error('读取图片失败')),
    })
  })
  // #endif

  throw new Error('当前平台暂不支持图片读取')
}
