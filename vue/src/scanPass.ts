export function tokenFromScan(value: string, origin: string): string | null {
  try {
    const url = new URL(value)
    return url.origin === origin && url.pathname === '/scan' && url.hash.length > 1
      ? url.hash.slice(1)
      : null
  } catch {
    return null
  }
}
