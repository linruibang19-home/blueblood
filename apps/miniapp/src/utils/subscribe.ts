/**
 * 订阅消息封装（微信小程序原生能力）
 * 注意：仅在小程序/真机可用，H5 无效。
 */

export interface SubscribeResult {
  /** 模板 ID -> 授权状态：'accept' | 'reject' | 'ban' | 'filter' */
  [tmplId: string]: string
}

/**
 * 请求订阅消息授权（前端仅订阅动作，后端下发消息留 TODO）
 * @param tmplIds 模板 ID 列表
 * @returns 授权结果 map
 */
export function requestSubscribe(tmplIds: string[]): Promise<SubscribeResult> {
  return new Promise<SubscribeResult>((resolve, reject) => {
    uni.requestSubscribeMessage({
      tmplIds,
      success: (res: any) => {
        // res 形如 { 'tmplId': 'accept', errMsg: '...' }
        const out: SubscribeResult = {}
        for (const id of tmplIds) {
          if (res && res[id]) out[id] = res[id]
        }
        resolve(out)
      },
      fail: (err: any) => {
        reject(new Error(err?.errMsg || '订阅失败'))
      },
    })
  })
}

/**
 * 订阅 + 后续通知的预留入口。
 * 当前仅执行前端订阅动作；后端实际下发消息的调用点由业务方在合适时机触发（TODO）。
 * @param tmplIds 模板 ID 列表
 */
export async function subscribeAndNotify(tmplIds: string[]): Promise<SubscribeResult> {
  // TODO: 后端推送消息（如审核结果通知）在此或业务流程中触发
  return requestSubscribe(tmplIds)
}
