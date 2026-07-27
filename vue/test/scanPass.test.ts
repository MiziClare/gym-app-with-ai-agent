import assert from 'node:assert/strict'
import test from 'node:test'
import { tokenFromScan } from '../src/scanPass.ts'

test('accepts only this application membership scan links', () => {
  const origin = 'https://gym.example'
  assert.equal(tokenFromScan(`${origin}/scan#signed-pass`, origin), 'signed-pass')
  assert.equal(tokenFromScan('https://attacker.example/scan#signed-pass', origin), null)
  assert.equal(tokenFromScan(`${origin}/other#signed-pass`, origin), null)
  assert.equal(tokenFromScan('not a URL', origin), null)
})
