import http from '@/utils/request'

export interface Group {
  id: string
  name: string
  avatar: string
  description: string
  memberCount: number
  tag: string
  joined: boolean
}

interface PageResp {
  list?: any[]
}

function mapGroup(raw: any): Group {
  return {
    id: String(raw?.id ?? ''),
    name: raw?.name || '',
    avatar: raw?.avatar || raw?.coverImage || '',
    description: raw?.description || '',
    memberCount: Number(raw?.memberCount ?? raw?.members ?? 0),
    tag: raw?.tag || '',
    joined: !!raw?.joined,
  }
}

function fromList(data: any): any[] {
  return Array.isArray(data) ? data : (data as PageResp)?.list ?? []
}

/** GET /group/mine */
export async function getMyGroups(): Promise<Group[]> {
  const data = await http.get<any>('/group/mine').catch(() => http.get<any>('/group'))
  return fromList(data).map(mapGroup)
}

/** GET /group */
export async function getGroups(): Promise<Group[]> {
  const data = await http.get<any>('/group').catch(() => ({} as any))
  return fromList(data).map(mapGroup)
}
