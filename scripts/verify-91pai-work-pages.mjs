import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8')
}

function assertContains(file, text) {
  const content = read(file)
  if (!content.includes(text)) {
    throw new Error(`${file} 缺少 ${text}`)
  }
}

function assertExists(file) {
  if (!fs.existsSync(path.join(root, file))) {
    throw new Error(`${file} 不存在`)
  }
}

const apiFile = '91pai-master/common/vmeitime-http/index.js'
const apiExports = [
  'export const banner_list',
  'export const work_get',
  'export const work_like_add',
  'export const work_like_cancel',
  'export const work_sc_add',
  'export const work_sc_cancel',
  'export const work_quick_apply',
  'export const work_complain',
]

apiExports.forEach((item) => assertContains(apiFile, item))

const pages = [
  '91pai-master/pages/work/detail/index.vue',
  '91pai-master/pages/work/detail/index.js',
  '91pai-master/pages/work/detail/index.scss',
  '91pai-master/pages/work/complain/index.vue',
  '91pai-master/pages/work/complain/index.js',
  '91pai-master/pages/work/complain/index.scss',
  '91pai-master/pages/work/apply/index.vue',
  '91pai-master/pages/work/apply/index.js',
  '91pai-master/pages/work/apply/index.scss',
  '91pai-master/pages/mine/realname/intro/index.vue',
  '91pai-master/pages/mine/realname/intro/index.js',
  '91pai-master/pages/mine/realname/intro/index.scss',
  '91pai-master/components/custom/homeBanner/index.vue',
  '91pai-master/components/custom/splashOverlay/index.vue',
]

pages.forEach(assertExists)

assertContains('91pai-master/pages.json', '"path": "pages/work/detail/index"')
assertContains('91pai-master/pages.json', '"path": "pages/work/complain/index"')
assertContains('91pai-master/pages.json', '"path": "pages/work/apply/index"')
assertContains('91pai-master/pages.json', '"path": "pages/mine/realname/intro/index"')
assertContains('91pai-master/pages/home/home/index.vue', '<home-banner')
assertContains('91pai-master/pages/home/home/index.vue', '<splash-overlay')
assertContains('91pai-master/pages/work/complain/index.vue', '请选择投诉原因')
assertContains('91pai-master/pages/work/apply/index.vue', '安全防骗提醒')
assertContains('91pai-master/pages/mine/realname/intro/index.vue', '开始实名')

console.log('91pai work pages verification passed')
