import assert from 'node:assert/strict'
import test from 'node:test'
import { filterEquipment, type EquipmentAvailability } from '../src/equipmentAvailability.ts'

const items: EquipmentAvailability[] = [
  {
    id: 1,
    name: 'Treadmill',
    category: 'Cardio',
    description: '',
    unitLabel: 'machines',
    totalUnits: 12,
    availableUnits: 8,
    availabilityStatus: 'LIMITED',
    locations: ['Main Floor · Cardio Zone'],
    updatedAt: null,
  },
  {
    id: 2,
    name: 'Lap Pool Lane',
    category: 'Aquatics & Recovery',
    description: '',
    unitLabel: 'lanes',
    totalUnits: 6,
    availableUnits: 6,
    availabilityStatus: 'AVAILABLE',
    locations: ['Main Floor · Pool & Recovery'],
    updatedAt: null,
  },
]

test('filters walk-in availability by search, category and status', () => {
  assert.deepEqual(filterEquipment(items, 'pool', 'All', 'ALL').map(item => item.id), [2])
  assert.deepEqual(filterEquipment(items, '', 'Cardio', 'LIMITED').map(item => item.id), [1])
  assert.deepEqual(filterEquipment(items, 'main floor', 'All', 'AVAILABLE').map(item => item.id), [2])
})
