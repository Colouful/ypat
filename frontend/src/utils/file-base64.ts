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

  // #ifdef MP-WEIXIN
  return new Promise((resolve, reject) => {
    uni.getFileSystemManager().readFile({
      filePath,
      encoding: 'base64',
      success: (result) => resolve(String(result.data || '')),
      fail: () => reject(new Error('读取图片失败')),
    })
  })
  // #endif

  // #ifdef H5
  const response = await fetch(filePath)
  if (!response.ok) throw new Error('读取图片失败')
  return blobToBase64(await response.blob())
  // #endif

  // #ifdef APP-PLUS
  return new Promise((resolve, reject) => {
    plus.io.resolveLocalFileSystemURL(filePath, (entry) => {
      entry.file((file) => {
        const reader = new plus.io.FileReader()
        reader.onloadend = (event) => resolve(removeDataUrlHeader(String(event.target?.result || '')))
        reader.onerror = () => reject(new Error('读取图片失败'))
        reader.readAsDataURL(file)
      }, () => reject(new Error('读取图片失败')))
    }, () => reject(new Error('读取图片失败')))
  })
  // #endif

  throw new Error('当前平台暂不支持图片读取')
}
