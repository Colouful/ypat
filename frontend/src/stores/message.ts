import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useMessageStore = defineStore('message', () => {
  const recUnreadCount = ref<number>(0)
  const sendUnreadCount = ref<number>(0)
  const totalUnread = computed(() => recUnreadCount.value + sendUnreadCount.value)

  async function refreshCounts(userid: string) {
    try {
      const res = await uni.$http.get<{
        recUnreadCount: number
        sendUnreadCount: number
      }>('/message/unread-counts', { userid })
      if (res.data) {
        recUnreadCount.value = res.data.recUnreadCount
        sendUnreadCount.value = res.data.sendUnreadCount
      }
    } catch {
      // silently fail
    }
  }

  return {
    recUnreadCount,
    sendUnreadCount,
    totalUnread,
    refreshCounts,
  }
})
