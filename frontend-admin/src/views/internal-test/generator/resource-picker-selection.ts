import type { InternalTestResource, InternalTestResourceGroup } from '@/api/modules/internal-test'

export const YPAT_RESOURCE_LIMIT = 9

export function replaceWorkGroupSelection(
  group: InternalTestResourceGroup,
  checked: boolean,
): InternalTestResourceGroup | undefined {
  return checked ? group : undefined
}

function isSameResource(candidate: InternalTestResource, resource: InternalTestResource): boolean {
  return resource.id === undefined ? candidate === resource : candidate.id === resource.id
}

export function toggleYpatResourceSelection(
  selected: InternalTestResource[],
  resource: InternalTestResource,
  checked: boolean,
): { selected: InternalTestResource[]; limitReached: boolean } {
  if (!checked) {
    return {
      selected: selected.filter((candidate) => !isSameResource(candidate, resource)),
      limitReached: false,
    }
  }

  if (selected.some((candidate) => isSameResource(candidate, resource))) {
    return { selected: [...selected], limitReached: false }
  }

  if (selected.length >= YPAT_RESOURCE_LIMIT) {
    return { selected: [...selected], limitReached: true }
  }

  return { selected: [...selected, resource], limitReached: false }
}
