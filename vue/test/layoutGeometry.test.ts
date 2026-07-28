import assert from 'node:assert/strict'
import test from 'node:test'
import { moveBox, normalizeBox, resizeBox } from '../src/layoutGeometry.ts'

test('snaps and keeps layout boxes inside the floor', () => {
  assert.deepEqual(
    moveBox({ x: 10, y: 10, width: 30, height: 20 }, 70.6, -20),
    { x: 70, y: 0, width: 30, height: 20 },
  )
  assert.deepEqual(
    resizeBox({ x: 80, y: 90, width: 10, height: 5 }, 20, -10),
    { x: 80, y: 90, width: 20, height: 3 },
  )
  assert.deepEqual(
    normalizeBox({ x: 99, y: 99, width: 0, height: 200 }),
    { x: 97, y: 0, width: 3, height: 100 },
  )
})
