import type { DataSource, ParsedQuery, ParsedCondition, QueryOutput, CountOutput, ColumnTypeDef } from '@eaphone/query-core'
import initSqlJs, { type Database as SqlJsDatabase, type SqlValue, type BindParams } from 'sql.js'
import { readFile, writeFile } from 'fs/promises'

export interface SqliteDataSourceOptions {
  dbPath?: string
  dbBuffer?: Uint8Array
}

function sqliteTypeToColumnTypeDef(sqliteType: string): ColumnTypeDef {
  const t = sqliteType.toUpperCase()
  if (t.includes('INT')) return 'integer'
  if (t.includes('REAL') || t.includes('FLOAT') || t.includes('DOUBLE') || t.includes('NUMERIC')) return 'double'
  if (t.includes('BOOL')) return 'boolean'
  if (t.includes('DATE') || t.includes('TIME') || t.includes('TIMESTAMP')) return 'date'
  return 'string'
}

function toSqlValues(params: unknown[]): SqlValue[] {
  return params.map((p) => {
    if (p === null || p === undefined) return null
    if (typeof p === 'number' || typeof p === 'string') return p
    if (p instanceof Date) return p.toISOString()
    return String(p)
  })
}

export async function createSqliteDataSource(options: SqliteDataSourceOptions): Promise<DataSource> {
  const SQL = await initSqlJs()
  let db: SqlJsDatabase
  if (options.dbBuffer) {
    db = new SQL.Database(options.dbBuffer)
  } else if (options.dbPath) {
    let buffer: Buffer | undefined
    try {
      buffer = await readFile(options.dbPath)
    } catch {
      // File doesn't exist — create new DB
    }
    db = new SQL.Database(buffer ?? undefined)
  } else {
    db = new SQL.Database()
  }

  const tableSchemas = new Map<string, Map<string, ColumnTypeDef>>()

  function getSchema(table: string): Map<string, ColumnTypeDef> {
    let schema = tableSchemas.get(table)
    if (schema) return schema
    schema = new Map()
    try {
      const result = db.exec(`PRAGMA table_info('${table.replace(/'/g, "''")}')`)
      if (result.length > 0) {
        for (const row of result[0].values) {
          const name = String(row[1])
          const type = String(row[2] ?? 'TEXT')
          schema.set(name, sqliteTypeToColumnTypeDef(type))
        }
      }
    } catch {
      // pragma might fail if table doesn't exist
    }
    tableSchemas.set(table, schema)
    return schema
  }

  function escapeField(field: string): string {
    return `"${field.replace(/"/g, '""')}"`
  }

  function buildCondition(cond: ParsedCondition): { sql: string; params: SqlValue[] } {
    switch (cond.op) {
      case '_eq':
        return { sql: `${escapeField(cond.field)} = ?`, params: toSqlValues([cond.value]) }
      case '_ne':
        return { sql: `${escapeField(cond.field)} <> ?`, params: toSqlValues([cond.value]) }
      case '_gt':
        return { sql: `${escapeField(cond.field)} > ?`, params: toSqlValues([cond.value]) }
      case '_gte':
        return { sql: `${escapeField(cond.field)} >= ?`, params: toSqlValues([cond.value]) }
      case '_lt':
        return { sql: `${escapeField(cond.field)} < ?`, params: toSqlValues([cond.value]) }
      case '_lte':
        return { sql: `${escapeField(cond.field)} <= ?`, params: toSqlValues([cond.value]) }
      case '_like':
        return { sql: `${escapeField(cond.field)} LIKE ?`, params: toSqlValues([cond.value]) }
      case '_notlike':
        return { sql: `${escapeField(cond.field)} NOT LIKE ?`, params: toSqlValues([cond.value]) }
      case '_null':
        return { sql: `${escapeField(cond.field)} IS NULL`, params: [] }
      case '_empty':
        return { sql: `${escapeField(cond.field)} = ''`, params: [] }
      case '_isvoid':
        return { sql: `(${escapeField(cond.field)} IS NULL OR ${escapeField(cond.field)} = '')`, params: [] }
      case '_in': {
        const arr = cond.value as unknown[]
        const nonNull = arr.filter((v) => v !== null && v !== undefined)
        const hasNull = nonNull.length !== arr.length
        if (hasNull) {
          const placeholders = nonNull.map(() => '?').join(', ')
          if (nonNull.length === 0) {
            return { sql: `${escapeField(cond.field)} IS NULL`, params: [] }
          }
          return {
            sql: `(${escapeField(cond.field)} IS NULL OR ${escapeField(cond.field)} IN (${placeholders}))`,
            params: toSqlValues(nonNull),
          }
        }
        const placeholders = arr.map(() => '?').join(', ')
        return { sql: `${escapeField(cond.field)} IN (${placeholders})`, params: toSqlValues(arr) }
      }
      case '_nin': {
        const arr = cond.value as unknown[]
        const nonNull = arr.filter((v) => v !== null && v !== undefined)
        const hasNull = nonNull.length !== arr.length
        if (hasNull) {
          const placeholders = nonNull.map(() => '?').join(', ')
          if (nonNull.length === 0) {
            return { sql: `${escapeField(cond.field)} IS NOT NULL`, params: [] }
          }
          return {
            sql: `(${escapeField(cond.field)} IS NOT NULL AND ${escapeField(cond.field)} NOT IN (${placeholders}))`,
            params: toSqlValues(nonNull),
          }
        }
        const placeholders = arr.map(() => '?').join(', ')
        return { sql: `${escapeField(cond.field)} NOT IN (${placeholders})`, params: toSqlValues(arr) }
      }
      case '_regex':
        throw new Error('_regex is not supported in SQLite (use _like instead)')
      case '_all':
        throw new Error('_all is not supported in SQLite')
      case '_exists':
        throw new Error('_exists is not supported in SQLite (only for JSON columns)')
      default:
        throw new Error(`Operator ${cond.op} is not supported in SQLite`)
    }
  }

  function buildWhere(conditions: ParsedCondition[]): { sql: string; params: SqlValue[] } {
    if (conditions.length === 0) return { sql: '', params: [] }
    const parts = conditions.map((c) => buildCondition(c))
    const sql = ' WHERE ' + parts.map((p) => p.sql).join(' AND ')
    const params = parts.flatMap((p) => p.params)
    return { sql, params }
  }

  function buildOrderBy(sort: ParsedQuery['sort']): string {
    if (sort.length === 0) return ''
    const parts = sort.map((s) => `${escapeField(s.field)} ${s.dir === 'desc' ? 'DESC' : 'ASC'}`)
    return ' ORDER BY ' + parts.join(', ')
  }

  function execCount(sql: string, params?: SqlValue[]): number {
    try {
      const result = params ? db.exec(sql, params as BindParams) : db.exec(sql)
      return (result[0]?.values[0]?.[0] as number) ?? 0
    } catch {
      return 0
    }
  }

  async function query(table: string, input: ParsedQuery): Promise<QueryOutput> {
    getSchema(table)
    const where = buildWhere(input.conditions)
    const orderBy = buildOrderBy(input.sort)

    const countSql = `SELECT COUNT(*) AS cnt FROM ${escapeField(table)}`
    const total = execCount(countSql)

    let filtered = 0
    let data: Record<string, unknown>[] = []

    if (input.conditions.length === 0) {
      filtered = total
    } else {
      filtered = execCount(countSql + where.sql, where.params)
    }

    const dataSql = `SELECT * FROM ${escapeField(table)}${where.sql}${orderBy} LIMIT ? OFFSET ?`
    const allParams: SqlValue[] = [...where.params, input.limit, input.offset]
    const stmt = db.prepare(dataSql)
    try {
      stmt.bind(allParams as BindParams)
      while (stmt.step()) {
        data.push(stmt.getAsObject() as Record<string, unknown>)
      }
    } finally {
      stmt.free()
    }

    return { total, filtered, data }
  }

  async function count(table: string, conditions: ParsedCondition[]): Promise<CountOutput> {
    const where = buildWhere(conditions)
    const countSql = `SELECT COUNT(*) AS cnt FROM ${escapeField(table)}`
    const total = execCount(countSql)

    let filtered = total
    if (conditions.length > 0) {
      filtered = execCount(countSql + where.sql, where.params)
    }

    return { total, filtered }
  }

  async function close(): Promise<void> {
    if (options.dbPath) {
      const data = db.export()
      await writeFile(options.dbPath, Buffer.from(data))
    }
    db.close()
  }

  return { query, count, close }
}
