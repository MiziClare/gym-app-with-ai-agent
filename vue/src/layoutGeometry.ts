export type LayoutBox = { x: number; y: number; width: number; height: number }

export const snap = (value: number) => Math.round(value)

export function moveBox(box: LayoutBox, dx: number, dy: number): LayoutBox {
  return {
    ...box,
    x: snap(Math.max(0, Math.min(100 - box.width, box.x + dx))),
    y: snap(Math.max(0, Math.min(100 - box.height, box.y + dy))),
  }
}

export function resizeBox(box: LayoutBox, dw: number, dh: number): LayoutBox {
  return {
    ...box,
    width: snap(Math.max(3, Math.min(100 - box.x, box.width + dw))),
    height: snap(Math.max(3, Math.min(100 - box.y, box.height + dh))),
  }
}

export function normalizeBox(box: LayoutBox): LayoutBox {
  const width = snap(Math.max(3, Math.min(100, Number(box.width) || 3)))
  const height = snap(Math.max(3, Math.min(100, Number(box.height) || 3)))
  return {
    width,
    height,
    x: snap(Math.max(0, Math.min(100 - width, Number(box.x) || 0))),
    y: snap(Math.max(0, Math.min(100 - height, Number(box.y) || 0))),
  }
}
