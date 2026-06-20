// API - 企业模块
import request, { USE_API, ensureReady } from './request'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | string

export interface EnterpriseApplication {
  id?: number | string
  companyName: string
  creditCode: string
  licenseUrl: string
  contactName: string
  contactPhone: string
  status?: ApplicationStatus
  rejectReason?: string
  createdAt?: string
}

export interface Job {
  id?: number | string
  title: string
  company: string
  companyLogo?: string
  location: string
  salary: string
  type: string
  tags?: string[]
  description: string
  requirements?: string[]
  contact: string
  createdAt?: string
}

export interface Hackathon {
  id?: number | string
  title: string
  company?: string
  companyLogo?: string
  description: string
  startDate?: string
  endDate?: string
  location?: string
  prize?: string
  tags?: string[]
  requirements?: string[]
  contact?: string
  status?: string
  createdAt?: string
  [key: string]: any
}

/** 提交企业申请（已企业或已有 PENDING/APPROVED 会被拒） */
export async function submitEnterpriseApplication(payload: EnterpriseApplication): Promise<any> {
  if (USE_API) {
    await ensureReady()
    return request.post('/enterprise/application', payload)
  }
  await delay(400)
  return { ...payload, status: 'PENDING' }
}

/** 我的企业申请状态 */
export async function getMyApplication(): Promise<EnterpriseApplication | null> {
  if (USE_API) {
    await ensureReady()
    try {
      return await request.get<any, any>('/enterprise/my-application')
    } catch {
      return null
    }
  }
  await delay(200)
  return null
}

/** 发布岗位（仅 user_type=enterprise） */
export async function publishJob(payload: Job): Promise<Job> {
  if (USE_API) {
    await ensureReady()
    return request.post<any, Job>('/enterprise/jobs', payload)
  }
  await delay(400)
  return { ...payload, id: `job_${Date.now()}`, createdAt: new Date().toISOString() }
}

/** 我发布的岗位 */
export async function getMyJobs(): Promise<Job[]> {
  if (USE_API) {
    await ensureReady()
    try {
      const data = await request.get<any, any>('/enterprise/jobs')
      return Array.isArray(data) ? data : (data?.list || [])
    } catch {
      return []
    }
  }
  await delay(200)
  return []
}

export async function updateJob(id: number | string, payload: Partial<Job>): Promise<any> {
  if (USE_API) {
    await ensureReady()
    return request.put(`/enterprise/jobs/${id}`, payload)
  }
  await delay(300)
  return { ...payload, id }
}

export async function deleteJob(id: number | string): Promise<any> {
  if (USE_API) {
    await ensureReady()
    return request.delete(`/enterprise/jobs/${id}`)
  }
  await delay(200)
  return {}
}

/** 发布黑客松 */
export async function publishHackathon(payload: Hackathon): Promise<Hackathon> {
  if (USE_API) {
    await ensureReady()
    return request.post<any, Hackathon>('/enterprise/hackathons', payload)
  }
  await delay(400)
  return { ...payload, id: `hack_${Date.now()}`, createdAt: new Date().toISOString() }
}

export async function getMyHackathons(): Promise<Hackathon[]> {
  if (USE_API) {
    await ensureReady()
    try {
      const data = await request.get<any, any>('/enterprise/hackathons')
      return Array.isArray(data) ? data : (data?.list || [])
    } catch {
      return []
    }
  }
  await delay(200)
  return []
}

export async function updateHackathon(id: number | string, payload: Partial<Hackathon>): Promise<any> {
  if (USE_API) {
    await ensureReady()
    return request.put(`/enterprise/hackathons/${id}`, payload)
  }
  await delay(300)
  return { ...payload, id }
}

export async function deleteHackathon(id: number | string): Promise<any> {
  if (USE_API) {
    await ensureReady()
    return request.delete(`/enterprise/hackathons/${id}`)
  }
  await delay(200)
  return {}
}
