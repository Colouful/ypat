function isExplicitFlag(value?: string | null): value is '0' | '1' {
  return value === '0' || value === '1'
}

export function resolveYpatRealnameFlag(authorFlag?: string | null, detailFlag?: string | null): boolean {
  if (isExplicitFlag(authorFlag)) {
    return authorFlag === '1'
  }

  if (isExplicitFlag(detailFlag)) {
    return detailFlag === '1'
  }

  return false
}

export function resolveYpatCreditFlag(detailFlag?: string | null, authorFlag?: string | null): boolean {
  if (isExplicitFlag(detailFlag)) {
    return detailFlag === '1'
  }

  if (isExplicitFlag(authorFlag)) {
    return authorFlag === '1'
  }

  return false
}
