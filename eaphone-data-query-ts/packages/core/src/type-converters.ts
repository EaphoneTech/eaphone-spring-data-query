import type { ColumnTypeDef } from './types.js'

export function convertValue(value: unknown, typeDef?: ColumnTypeDef): unknown {
  if (value == null) return null
  switch (typeDef) {
    case 'string':
      return String(value)
    case 'integer': {
      const n = Number(value)
      return Number.isFinite(n) ? Math.floor(n) : null
    }
    case 'double': {
      const n = Number(value)
      return Number.isFinite(n) ? n : null
    }
    case 'boolean':
      return Boolean(value)
    case 'date':
      return parseDate(value)
    default:
      return value
  }
}

export function parseDate(value: unknown): Date | null {
  if (value instanceof Date) return value
  const s = String(value)
  const d = new Date(s)
  return Number.isNaN(d.getTime()) ? null : d
}

export function convertArray(
  arr: unknown[],
  typeDef?: ColumnTypeDef,
): unknown[] {
  return arr.map((v) => convertValue(v, typeDef))
}
