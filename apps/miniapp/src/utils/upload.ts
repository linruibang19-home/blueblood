import { BASE_URL, TOKEN_KEY } from '@/config'
import type { ApiResult } from '@/utils/request'

export interface UploadFileOptions {
  /** 本地文件路径（由 uni.chooseImage 等返回的 tempFilePaths） */
  filePath: string
  /** 业务类型，作为 formData.bizType 透传给后端 */
  bizType?: string
  /** 上传进度回调 */
  onProgress?: (percent: number) => void
}

export interface UploadResult {
  id?: string | number
  url: string
  [k: string]: any
}

/**
 * 上传单个文件到 POST /file/upload
 * - 使用 uni.uploadFile（小程序原生 multipart 上传）
 * - 自动注入 Authorization: Bearer <token>
 * - 解包 { code, message, data }，resolve data.url
 * 注意：微信原生 API，仅在小程序/真机可用，H5 不可用。
 */
export function uploadFile(options: UploadFileOptions): Promise<UploadResult> {
  const { filePath, bizType, onProgress } = options

  const token = uni.getStorageSync(TOKEN_KEY) as string
  const header: Record<string, string> = {}
  if (token) header.Authorization = `Bearer ${token}`

  const formData: Record<string, string> = {}
  if (bizType) formData.bizType = bizType

  return new Promise<UploadResult>((resolve, reject) => {
    const task = uni.uploadFile({
      url: BASE_URL + '/file/upload',
      filePath,
      name: 'file',
      formData,
      header,
      success: (res) => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(new Error(`上传失败 HTTP ${res.statusCode}`))
          return
        }
        let body: ApiResult<UploadResult> | undefined
        try {
          body = JSON.parse(res.data) as ApiResult<UploadResult>
        } catch (e) {
          reject(new Error('上传响应解析失败'))
          return
        }
        if (body && typeof body.code === 'number') {
          if (body.code === 0 || body.code === 200) {
            if (body.data && body.data.url) {
              resolve(body.data)
            } else {
              reject(new Error('上传成功但未返回 url'))
            }
            return
          }
          reject(new Error(body.message || `上传业务错误 ${body.code}`))
          return
        }
        reject(new Error('上传响应格式不正确'))
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '上传失败'))
      },
    })

    if (onProgress && task && typeof (task as any).onProgressUpdate === 'function') {
      ;(task as any).onProgressUpdate((p: any) => {
        onProgress(Number(p?.progress ?? 0))
      })
    }
  })
}

/**
 * 选择并上传多张图片，返回 url 列表。
 * @param count 最多选择数量（默认 1）
 * @param bizType 业务类型
 */
export async function chooseAndUploadImages(
  count = 1,
  bizType?: string
): Promise<UploadResult[]> {
  const choose = await uni.chooseImage({
    count,
    sizeType: ['compressed', 'original'],
    sourceType: ['album', 'camera'],
  })
  const tempFiles = (choose as any)?.tempFilePaths || (choose as any)?.tempFiles?.map((f: any) => f.path) || []
  if (!tempFiles.length) return []

  const results: UploadResult[] = []
  for (const p of tempFiles) {
    const r = await uploadFile({ filePath: p, bizType })
    results.push(r)
  }
  return results
}
