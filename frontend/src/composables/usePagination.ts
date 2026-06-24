import { ref, type Ref } from 'vue'

export interface PaginationOptions<T> {
  /** The async function to fetch data. Receives page and pageSize. */
  fetchData: (page: number, pageSize: number) => Promise<{ list: T[]; total: number }>
  /** Items per page, defaults to 10 */
  pageSize?: number
  /** Whether to load the first page immediately */
  immediate?: boolean
}

export interface PaginationResult<T> {
  list: Ref<T[]>
  loading: Ref<boolean>
  refreshing: Ref<boolean>
  hasMore: Ref<boolean>
  page: Ref<number>
  total: Ref<number>
  loadMore: () => Promise<void>
  refresh: () => Promise<void>
  reset: () => void
}

export function usePagination<T>(options: PaginationOptions<T>): PaginationResult<T> {
  const { fetchData, pageSize = 10, immediate = true } = options

  const list = ref<T[]>([]) as Ref<T[]>
  const loading = ref<boolean>(false)
  const refreshing = ref<boolean>(false)
  const hasMore = ref<boolean>(true)
  const page = ref<number>(0)
  const total = ref<number>(0)

  async function loadMore(): Promise<void> {
    if (loading.value || !hasMore.value) return

    loading.value = true
    try {
      const nextPage = page.value + 1
      const result = await fetchData(nextPage, pageSize)

      if (nextPage === 1) {
        list.value = result.list
      } else {
        list.value = [...list.value, ...result.list]
      }

      total.value = result.total
      page.value = nextPage

      // Determine if there are more pages
      hasMore.value = list.value.length < result.total
    } catch (error) {
      // If first page fails, mark no more to prevent infinite retries
      if (page.value === 0) {
        hasMore.value = false
      }
      throw error
    } finally {
      loading.value = false
    }
  }

  async function refresh(): Promise<void> {
    refreshing.value = true
    try {
      page.value = 0
      hasMore.value = true
      await loadMore()
    } finally {
      refreshing.value = false
    }
  }

  function reset(): void {
    list.value = []
    loading.value = false
    refreshing.value = false
    hasMore.value = true
    page.value = 0
    total.value = 0
  }

  // Auto-load first page if immediate
  if (immediate) {
    loadMore()
  }

  return {
    list,
    loading,
    refreshing,
    hasMore,
    page,
    total,
    loadMore,
    refresh,
    reset,
  }
}
