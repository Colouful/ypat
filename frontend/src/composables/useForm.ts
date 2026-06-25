import { ref, watch, type Ref } from 'vue'

export interface FormRule {
  required?: boolean
  message: string
  min?: number
  max?: number
  pattern?: RegExp
  validator?: (value: unknown) => boolean | Promise<boolean>
}

export type FormRules<T> = {
  [K in keyof T]?: FormRule[]
}

export interface FormOptions<T extends Record<string, unknown>> {
  /** Initial form data */
  initialData: T
  /** Validation rules */
  rules?: FormRules<T>
  /** Submit handler */
  onSubmit: (data: T) => Promise<void>
  /** Auto-save draft function (called on form change) */
  onDraftSave?: (data: T) => void
  /** Draft save debounce delay in ms, defaults to 1000 */
  draftDelay?: number
}

export interface FormResult<T> {
  formData: Ref<T>
  errors: Ref<Record<string, string>>
  submitting: Ref<boolean>
  validate: () => Promise<boolean>
  validateField: (field: keyof T) => Promise<boolean>
  submit: () => Promise<void>
  reset: () => void
  setFieldValue: (field: keyof T, value: unknown) => void
}

export function useForm<T extends Record<string, unknown>>(options: FormOptions<T>): FormResult<T> {
  const { initialData, rules, onSubmit, onDraftSave, draftDelay = 1000 } = options

  const formData = ref<T>({ ...initialData }) as Ref<T>
  const errors = ref<Record<string, string>>({})
  const submitting = ref<boolean>(false)

  let draftTimer: ReturnType<typeof setTimeout> | null = null

  if (onDraftSave) {
    watch(
      formData,
      (newData) => {
        if (draftTimer) clearTimeout(draftTimer)
        draftTimer = setTimeout(() => {
          onDraftSave(newData)
        }, draftDelay)
      },
      { deep: true },
    )
  }

  async function validateField(field: keyof T): Promise<boolean> {
    const fieldRules = rules?.[field]
    if (!fieldRules) return true

    const value = formData.value[field]

    for (const rule of fieldRules) {
      if (rule.required) {
        if (value === undefined || value === null || value === '') {
          errors.value[field as string] = rule.message
          return false
        }
        if (Array.isArray(value) && value.length === 0) {
          errors.value[field as string] = rule.message
          return false
        }
      }

      if (rule.min !== undefined && typeof value === 'string' && value.length < rule.min) {
        errors.value[field as string] = rule.message
        return false
      }

      if (rule.max !== undefined && typeof value === 'string' && value.length > rule.max) {
        errors.value[field as string] = rule.message
        return false
      }

      if (rule.pattern && typeof value === 'string' && !rule.pattern.test(value)) {
        errors.value[field as string] = rule.message
        return false
      }

      if (rule.validator) {
        const valid = await rule.validator(value)
        if (!valid) {
          errors.value[field as string] = rule.message
          return false
        }
      }
    }

    delete errors.value[field as string]
    return true
  }

  async function validate(): Promise<boolean> {
    errors.value = {}
    if (!rules) return true

    let allValid = true
    for (const field of Object.keys(rules) as Array<keyof T>) {
      const fieldValid = await validateField(field)
      if (!fieldValid) allValid = false
    }
    return allValid
  }

  async function submit(): Promise<void> {
    if (submitting.value) return

    const isValid = await validate()
    if (!isValid) {
      const firstError = Object.values(errors.value)[0]
      if (firstError) uni.showToast({ title: firstError, icon: 'none' })
      return
    }

    submitting.value = true
    try {
      await onSubmit(formData.value)
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : '提交失败，请重试'
      uni.showToast({ title: message, icon: 'none' })
      throw error
    } finally {
      submitting.value = false
    }
  }

  function reset(): void {
    formData.value = { ...initialData }
    errors.value = {}
    submitting.value = false
  }

  function setFieldValue(field: keyof T, value: unknown): void {
    const target = formData.value as Record<string, unknown>
    target[field as string] = value
  }

  return {
    formData,
    errors,
    submitting,
    validate,
    validateField,
    submit,
    reset,
    setFieldValue,
  }
}
