// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import WebViewPage from './web-view.vue'

const onLoadCallbacks = vi.hoisted(() => [] as Array<(query?: Record<string, unknown>) => void>)

vi.mock('@dcloudio/uni-app', () => ({
  onLoad: (callback: (query?: Record<string, unknown>) => void) => {
    onLoadCallbacks.push(callback)
  },
}))

const webViewStub = {
  name: 'web-view',
  props: ['src', 'fullscreen'],
  emits: ['load', 'error'],
  template: '<div class="web-view-stub" :data-src="src" @click="$emit(\'load\')" @dblclick="$emit(\'error\')" />',
}

function mountPage() {
  return mount(WebViewPage, {
    global: {
      stubs: {
        webView: webViewStub,
      },
    },
  })
}

async function loadPage(query?: Record<string, unknown>) {
  const callback = onLoadCallbacks[onLoadCallbacks.length - 1]
  callback?.(query)
  await nextTick()
}

describe('content web-view page', () => {
  beforeEach(() => {
    onLoadCallbacks.length = 0
    Object.assign(uni, {
      setClipboardData: vi.fn(),
      showToast: vi.fn(),
    })
  })

  it('shows fallback for invalid urls and keeps copy target', async () => {
    const wrapper = mountPage()

    await loadPage({ url: encodeURIComponent('ftp://example.com/a') })

    expect(wrapper.find('.web-view-stub').exists()).toBe(false)
    expect(wrapper.text()).toContain('无法打开链接')
    expect(wrapper.text()).toContain('仅支持 http:// 或 https:// 开头的链接')
    expect(wrapper.text()).toContain('ftp://example.com/a')
  })

  it('unmounts web-view and shows load failure fallback on error', async () => {
    const wrapper = mountPage()

    await loadPage({ url: encodeURIComponent('https://example.com/a') })
    expect(wrapper.find('.web-view-stub').exists()).toBe(true)

    await wrapper.find('.web-view-stub').trigger('dblclick')
    await nextTick()

    expect(wrapper.find('.web-view-stub').exists()).toBe(false)
    expect(wrapper.text()).toContain('页面加载失败')
    expect(wrapper.text()).toContain('当前页面暂时无法打开，请复制链接后在浏览器中访问')
    expect(wrapper.text()).toContain('https://example.com/a')
  })

  it('keeps a copy fallback action while a valid web-view is mounted', async () => {
    const wrapper = mountPage()

    await loadPage({ url: encodeURIComponent('https://example.com/a') })
    await wrapper.find('.web-view-page__copy-button').trigger('tap')

    expect(uni.setClipboardData).toHaveBeenCalledWith(expect.objectContaining({
      data: 'https://example.com/a',
    }))
  })
})
