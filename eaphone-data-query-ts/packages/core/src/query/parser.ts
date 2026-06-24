import type {
  QueryInput,
  QueryFilter,
  QueryOrder,
  ParsedCondition,
  ParsedSort,
  ParsedQuery,
  FilterOperator,
  ColumnTypeDef,
} from '../types.js'
import { convertValue } from '../type-converters.js'

export function parseOrderBy(orderBy?: QueryOrder[]): ParsedSort[] {
  if (!orderBy || orderBy.length === 0) return []
  const result: ParsedSort[] = []
  for (const entry of orderBy) {
    if (entry.field && entry.dir) {
      result.push({ field: entry.field, dir: entry.dir })
    } else {
      for (const [key, value] of Object.entries(entry)) {
        if (key === 'field' || key === 'dir') continue
        if (value === 'asc' || value === 'desc') {
          result.push({ field: key, dir: value })
        }
      }
    }
  }
  return result
}

const OPERATORS_TO_CHECK: { op: FilterOperator; key: keyof QueryFilter }[] = [
  { op: '_eq', key: '_eq' },
  { op: '_ne', key: '_ne' },
  { op: '_isvoid', key: '_isvoid' },
  { op: '_gt', key: '_gt' },
  { op: '_gte', key: '_gte' },
  { op: '_lt', key: '_lt' },
  { op: '_lte', key: '_lte' },
  { op: '_in', key: '_in' },
  { op: '_nin', key: '_nin' },
  { op: '_all', key: '_all' },
  { op: '_regex', key: '_regex' },
  { op: '_like', key: '_like' },
  { op: '_notlike', key: '_notlike' },
  { op: '_null', key: '_null' },
  { op: '_empty', key: '_empty' },
  { op: '_exists', key: '_exists' },
]

export function parseFilter(
  field: string,
  filter: QueryFilter,
  schema?: Record<string, ColumnTypeDef>,
): ParsedCondition[] {
  const typeDef = schema?.[field] ?? filter.type
  const conditions: ParsedCondition[] = []

  if (filter._eq !== undefined) {
    conditions.push({ field, op: '_eq', value: convertValue(filter._eq, typeDef) })
    return conditions
  }

  if (filter._ne !== undefined) {
    conditions.push({ field, op: '_ne', value: convertValue(filter._ne, typeDef) })
    return conditions
  }

  if (filter._isvoid !== undefined) {
    conditions.push({ field, op: '_isvoid', value: filter._isvoid })
    return conditions
  }

  for (const { op, key } of OPERATORS_TO_CHECK) {
    if (op === '_eq' || op === '_ne' || op === '_isvoid') continue
    const raw = filter[key]
    if (raw === undefined || raw === null) continue

    if (op === '_in' || op === '_nin') {
      if (Array.isArray(raw)) {
        conditions.push({ field, op, value: convertArrayWithNull(raw, typeDef) })
      }
    } else if (op === '_all') {
      if (Array.isArray(raw)) {
        conditions.push({ field, op, value: raw.map((v) => convertValue(v, typeDef)) })
      }
    } else if (op === '_null' || op === '_empty' || op === '_exists') {
      if (typeof raw === 'boolean') {
        conditions.push({ field, op, value: raw })
      }
    } else if (op === '_regex' || op === '_like' || op === '_notlike') {
      conditions.push({ field, op, value: String(raw) })
    } else {
      conditions.push({ field, op, value: convertValue(raw, typeDef) })
    }
  }

  return conditions
}

export interface NullSplitResult {
  hasNull: boolean
  values: unknown[]
}

export function splitNull(arr: unknown[]): NullSplitResult {
  const values = arr.filter((v) => v !== null && v !== undefined)
  return { hasNull: values.length !== arr.length, values }
}

function convertArrayWithNull(
  arr: unknown[],
  typeDef?: ColumnTypeDef,
): unknown[] {
  return arr.map((v) => (v === null ? null : convertValue(v, typeDef)))
}

export function parseQueryInput(
  input: QueryInput,
  schema?: Record<string, ColumnTypeDef>,
): ParsedQuery {
  const offset = input.offset ?? 0
  const limit = input.limit ?? 10
  const sort = parseOrderBy(input.order_by)
  const conditions: ParsedCondition[] = []

  if (input.where) {
    for (const [field, filter] of Object.entries(input.where)) {
      conditions.push(...parseFilter(field, filter, schema))
    }
  }

  return { draw: input.draw, offset, limit, sort, conditions }
}

export function parseCountInput(
  input: QueryInput,
  schema?: Record<string, ColumnTypeDef>,
): ParsedCondition[] {
  const conditions: ParsedCondition[] = []
  if (input.where) {
    for (const [field, filter] of Object.entries(input.where)) {
      conditions.push(...parseFilter(field, filter, schema))
    }
  }
  return conditions
}
