import { describe, expect, it } from 'vitest'
import { toRegionFields } from '../region'

describe('地区级联字段转换', () => {
  it('应将省市区级联路径拆成提交字段', () => {
    expect(toRegionFields(['浙江省', '杭州市', '西湖区'])).toEqual({
      province: '浙江省',
      city: '杭州市',
      area: '西湖区',
    })
  })

  it('只选择到城市时应保留省市并清空地区', () => {
    expect(toRegionFields(['上海市', '上海市'])).toEqual({
      province: '上海市',
      city: '上海市',
      area: '',
    })
  })

  it('清空选择时应清空省市区', () => {
    expect(toRegionFields([])).toEqual({
      province: '',
      city: '',
      area: '',
    })
  })

  it('级联组件清空为空值时应清空省市区', () => {
    expect(toRegionFields(null)).toEqual({
      province: '',
      city: '',
      area: '',
    })
    expect(toRegionFields(undefined)).toEqual({
      province: '',
      city: '',
      area: '',
    })
  })
})
