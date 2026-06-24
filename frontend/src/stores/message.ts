import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as messageApi from '@/api/modules/message'

export const useMessageStore = defineStore('message', () => {
  const recUnreadCount = ref(0)
  const sendUnreadCount = ref(0)
  const totalUnread = computed(() => recUnreadCount.value + sendUnreadCount.value)

  async function refreshCounts(userid: number, type = '0'): Promise<void> {
    try {
      const [received, sent] = await Promise.all([
        messageApi.getRecUnreadCount(type, userid),
        messageApi.getSendUnreadCount(type, userid),
      ])
      recUnreadCount.value = Number(received.data || 0)
      sendUnreadCount.value = Number(sent.data || 0)
    } catch {
      recUnreadCount.value = 0
      sendUnreadCount.value = 0
    }
  }

  return { recUnreadCount, sendUnreadCount, totalUnread, refreshCounts }
})
