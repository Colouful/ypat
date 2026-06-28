export function normalizePositiveIntegerInput(value: unknown): string {
  const integerPart = String(value ?? '').split(/[.。]/)[0]
  return integerPart.replace(/\D/g, '').replace(/^0+/, '')
}

export function toPositiveIntegerAmount(value: string): number | undefined {
  const amount = Number(normalizePositiveIntegerInput(value))
  return Number.isInteger(amount) && amount > 0 ? amount : undefined
}
