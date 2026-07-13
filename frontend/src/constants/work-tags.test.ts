import { describe, expect, it } from 'vitest'
import { resolveYpatTopicTags } from './work-tags'

const tagOptions = [
  { id: 1, code: 'qinglv', name: '情侣' },
  { id: 25, code: 'jiaopian', name: '胶片' },
  { id: 29, code: 'fugu', name: '复古' },
]

describe('resolveYpatTopicTags', () => {
  it('按约拍记录中的标签 ID 顺序解析主题标签名称', () => {
    expect(resolveYpatTopicTags('25,1,29', 'INS,情侣,复古', tagOptions)).toEqual([
      '胶片',
      '情侣',
      '复古',
    ])
  })

  it('标签字典不可用时回退到后端标签文案并去重', () => {
    expect(resolveYpatTopicTags('25,29', '胶片, 复古,胶片', [])).toEqual(['胶片', '复古'])
  })
})
