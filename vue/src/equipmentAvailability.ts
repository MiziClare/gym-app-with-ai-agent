export type AvailabilityStatus = 'AVAILABLE' | 'LIMITED' | 'UNAVAILABLE'

export type EquipmentAvailability = {
  id: number
  name: string
  category: string
  description: string
  unitLabel: string
  totalUnits: number
  availableUnits: number
  availabilityStatus: AvailabilityStatus
  locations: string[]
  updatedAt: string | null
}

export function filterEquipment(
  items: EquipmentAvailability[],
  search: string,
  category: string,
  status: 'ALL' | AvailabilityStatus,
) {
  const query = search.toLowerCase()
  return items.filter(item =>
    (category === 'All' || item.category === category)
    && (status === 'ALL' || item.availabilityStatus === status)
    && `${item.name} ${item.category} ${item.locations.join(' ')}`.toLowerCase().includes(query),
  )
}
