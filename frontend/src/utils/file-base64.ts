function removeDataUrlHeader(value: string): string {
  const index = value.indexOf(',')
  return value.startsWith('data:') && index >= 0 ? value.slice(index + 1) : value
}

const DATA_URL_PREFIX = 'data:image/jpeg;base64,'

/**
 * 返回带 dataURL 头的 base64。后端(/ypat/submit、/oauth/add、/user/upd)以
 * `data:...;base64,xxx` 形式接收图片(见 backend MockTest 与旧版实现),
 * 因此上传前必须带前缀,否则按 `,` 切分会取不到图片数据。
 */
export async function filePathToDataUrl(filePath: string): Promise<string> {
  return `${DATA_URL_PREFIX}${await filePathToBase64(filePath)}`
}

/** 确保值带 dataURL 头:已带则原样返回,裸 base64 则补前缀,路径则转换。 */
export async function ensureImageDataUrl(value: string): Promise<string> {
  if (!value) return value
  if (value.startsWith('data:')) return value
  // 裸 base64(无路径协议):直接补前缀;否则当作文件路径转换。
  if (/^[A-Za-z0-9+/=\r\n]+$/.test(value) && value.length > 200) {
    return `${DATA_URL_PREFIX}${value}`
  }
  return filePathToDataUrl(value)
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
