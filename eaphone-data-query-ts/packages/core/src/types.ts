/* ───────── DTO types ───────── */

export interface QueryInput {
  draw?: number
  offset?: number
  limit?: number
  order_by?: QueryOrder[]
  where?: Record<string, QueryFilter>
}

export interface QueryFilter {
  _eq?: unknown
  _ne?: unknown
  _gt?: unknown
  _gte?: unknown
  _lt?: unknown
  _lte?: unknown
  _in?: unknown[]
  _nin?: unknown[]
  _all?: unknown[]
  _regex?: string
  _like?: string
  _notlike?: string
  _null?: boolean
  _empty?: boolean
  _isvoid?: boolean
  _exists?: boolean
  type?: ColumnTypeDef
}

export interface QueryOrder {
  field?: string
  dir?: 'asc' | 'desc'
  [key: string]: unknown
}

export interface QueryOutput<T = Record<string, unknown>> {
  draw?: number
  total: number
  filtered: number
  data: T[]
  error?: string
}

export interface CountInput {
  where?: Record<string, QueryFilter>
}

export interface CountOutput {
  total: number
  filtered: number
  error?: string
}

/* ───────── Operator type ───────── */

export type FilterOperator =
  | '_eq' | '_ne'
  | '_gt' | '_gte' | '_lt' | '_lte'
  | '_in' | '_nin' | '_all'
  | '_regex' | '_like' | '_notlike'
  | '_null' | '_empty' | '_isvoid' | '_exists'

/* ───────── Column type ───────── */

export type ColumnTypeDef = 'string' | 'integer' | 'double' | 'date' | 'boolean'

/* ───────── Parsed internal query ───────── */

export interface ParsedCondition {
  field: string
  op: FilterOperator
  value: unknown
}

export interface ParsedSort {
  field: string
  dir: 'asc' | 'desc'
}

export interface ParsedQuery {
  draw?: number
  offset: number
  limit: number
  conditions: ParsedCondition[]
  sort: ParsedSort[]
}

/* ───────── DataSource plugin interface ───────── */

export interface DataSource {
  query(table: string, input: ParsedQuery): Promise<QueryOutput>
  count(table: string, conditions: ParsedCondition[]): Promise<CountOutput>
  close(): Promise<void>
}

/* ───────── Middleware configuration ───────── */

export interface ResourceConfig {
  path: string
  table: string
  schema?: Record<string, ColumnTypeDef>
}

export interface EaphoneQueryOptions {
  baseContext: string
  datasource: DataSource
  resources: ResourceConfig[]
}
