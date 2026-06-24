import './index'

declare module './index' {
  export interface AreaInfo {
    id?: number
    name?: string
  }

  export interface ParamInfo {
    id?: number
    name?: string
    value?: string
  }
}

export {}
