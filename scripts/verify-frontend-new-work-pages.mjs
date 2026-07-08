import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8')
}

function assertExists(file) {
  if (!fs.existsSync(path.join(root, file))) {
    throw new Error(`${file} 不存在`)
  }
}

function assertContains(file, text) {
  const content = read(file)
  if (!content.includes(text)) {
    throw new Error(`${file} 缺少 ${text}`)
  }
}

const files = [
  'AGEMT.md',
  'frontend/src/components/business/HomeBanner.vue',
  'frontend/src/components/business/SplashOverlay.vue',
  'frontend/src/pages-sub/work/complain.vue',
  'frontend/src/pages-sub/work/apply.vue',
  'frontend/src/pages-sub/user/realname-intro.vue',
  'frontend-admin/src/views/manage/work-complain-list/index.vue',
]

files.forEach(assertExists)

assertContains('AGEMT.md', '默认只修改 `frontend` 新版小程序')
assertContains('AGEMT.md', '默认只修改 `frontend-admin` 新版管理后台')
assertContains('AGEMT.md', '禁止把新版需求实现到 `91pai-master`')

assertContains('frontend/src/pages/home/index.vue', '<HomeBanner />')
assertContains('frontend/src/pages/home/index.vue', '<SplashOverlay />')
assertContains('frontend/src/components/business/HomeBanner.vue', 'contentApi.getBannerList')
assertContains('frontend/src/components/business/SplashOverlay.vue', '跳过 {{ countdown }}s')

assertContains('frontend/src/pages.json', '"path": "complain"')
assertContains('frontend/src/pages.json', '"path": "apply"')
assertContains('frontend/src/pages.json', '"path": "realname-intro"')

assertContains('frontend/src/pages-sub/work/detail.vue', '/pages-sub/work/complain?workId=')
assertContains('frontend/src/pages-sub/work/detail.vue', '/pages-sub/work/apply?workId=')
assertContains('frontend/src/pages-sub/work/complain.vue', '投诉原因')
assertContains('frontend/src/pages-sub/work/complain.vue', '证据截图')
assertContains('frontend/src/pages-sub/work/complain.vue', '拉黑确认')
assertContains('frontend/src/pages-sub/work/apply.vue', '安全防骗提醒')
assertContains('frontend/src/pages-sub/work/apply.vue', '拍拍豆')
assertContains('frontend/src/api/types/work.ts', 'interface WorkQuickApplyParams')
assertContains('frontend/src/api/types/work.ts', 'content?: string')
assertContains('frontend/src/api/types/work.ts', 'pics?: string')

assertContains('frontend/src/pages-sub/user/realname-intro.vue', '开始实名')
assertContains('frontend/src/pages/mine/index.vue', '/pages-sub/user/realname-intro')
assertContains('frontend/src/api/request.ts', '/pages-sub/user/realname-intro')
assertContains('frontend/src/composables/useAuth.ts', '/pages-sub/user/realname-intro')

assertContains('frontend-admin/src/constants/menu.ts', '作品投诉')
assertContains('frontend-admin/src/api/modules/work-complain.ts', '/admin/work/complain/list')

console.log('frontend new work pages verification passed')
