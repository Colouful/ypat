import chinaRegionOptions from '@/constants/china-region-options.json'

export interface RegionFields {
  province: string
  city: string
  area: string
}

export type RegionPath = string[]

export const regionCascaderOptions = chinaRegionOptions

export function toRegionFields(path: RegionPath): RegionFields {
  const [province = '', city = '', area = ''] = path

  return {
    province,
    city,
    area,
  }
}
