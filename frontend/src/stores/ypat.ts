import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface DraftForm {
  title: string
  content: string
  images: string[]
  category: string
  price: number | null
  contactWay: string
  location: string
  tags: string[]
  [key: string]: unknown
}

export interface FilterState {
  category: string
  sortBy: string
  city: string
  priceRange: [number, number] | null
  keyword: string
}

const defaultDraftForm: DraftForm = {
  title: '',
  content: '',
  images: [],
  category: '',
  price: null,
  contactWay: '',
  location: '',
  tags: [],
}

const defaultFilter: FilterState = {
  category: '',
  sortBy: 'latest',
  city: '',
  priceRange: null,
  keyword: '',
}

export const useYpatStore = defineStore(
  'ypat',
  () => {
    const draftForm = ref<DraftForm>({ ...defaultDraftForm })
    const currentFilter = ref<FilterState>({ ...defaultFilter })

    function saveDraft(form: Partial<DraftForm>) {
      draftForm.value = { ...draftForm.value, ...form }
    }

    function clearDraft() {
      draftForm.value = { ...defaultDraftForm }
    }

    function setFilter(filter: Partial<FilterState>) {
      currentFilter.value = { ...currentFilter.value, ...filter }
    }

    return {
      draftForm,
      currentFilter,
      saveDraft,
      clearDraft,
      setFilter,
    }
  },
  {
    persist: {
      paths: ['draftForm'],
      storage: {
        getItem(key: string) {
          return uni.getStorageSync(key) || null
        },
        setItem(key: string, value: string) {
          uni.setStorageSync(key, value)
        },
      },
    },
  }
)
