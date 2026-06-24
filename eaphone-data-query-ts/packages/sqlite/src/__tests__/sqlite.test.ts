import { describe, it, expect, beforeAll } from 'vitest'
import initSqlJs from 'sql.js'
import { createSqliteDataSource } from '../index.js'
import type { DataSource, ParsedQuery } from '@eaphone/query-core'

async function createTestDb(): Promise<Uint8Array> {
  const SQL = await initSqlJs()
  const db = new SQL.Database()
  db.run(`
    CREATE TABLE t_order (
      id INTEGER PRIMARY KEY,
      name TEXT NOT NULL,
      status TEXT,
      price REAL,
      created_at TEXT
    )
  `)
  db.run("INSERT INTO t_order VALUES (1, 'Alpha', 'valid', 10.5, '2024-01-15')")
  db.run("INSERT INTO t_order VALUES (2, 'Beta', 'valid', 20.0, '2024-02-20')")
  db.run("INSERT INTO t_order VALUES (3, 'Gamma', 'deleted', 5.0, '2024-03-10')")
  db.run("INSERT INTO t_order VALUES (4, 'Delta', 'valid', 30.0, NULL)")
  db.run("INSERT INTO t_order VALUES (5, 'Epsilon', '', 15.0, '2024-05-01')")
  const data = db.export()
  db.close()
  return data
}

let ds: DataSource

beforeAll(async () => {
  const buffer = await createTestDb()
  ds = await createSqliteDataSource({ dbBuffer: buffer })
})

describe('SqliteDataSource', () => {
  it('returns total count', async () => {
    const result = await ds.count('t_order', [])
    expect(result.total).toBe(5)
    expect(result.filtered).toBe(5)
  })

  it('returns all rows with empty query', async () => {
    const result = await ds.query('t_order', { offset: 0, limit: 10, conditions: [], sort: [] })
    expect(result.total).toBe(5)
    expect(result.filtered).toBe(5)
    expect(result.data).toHaveLength(5)
  })

  it('supports _eq filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'name', op: '_eq', value: 'Alpha' }],
    })
    expect(result.filtered).toBe(1)
    expect(result.data[0].name).toBe('Alpha')
  })

  it('supports _ne filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'status', op: '_ne', value: 'deleted' }],
    })
    expect(result.filtered).toBe(4)
  })

  it('supports _gt filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'price', op: '_gt', value: 15 }],
    })
    expect(result.filtered).toBe(2)
  })

  it('supports _gte filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'price', op: '_gte', value: 15 }],
    })
    expect(result.filtered).toBe(3)
  })

  it('supports _lt filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'price', op: '_lt', value: 15 }],
    })
    expect(result.filtered).toBe(2)
  })

  it('supports _lte filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'price', op: '_lte', value: 15 }],
    })
    expect(result.filtered).toBe(3)
  })

  it('supports _in filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'status', op: '_in', value: ['valid', 'deleted'] }],
    })
    expect(result.filtered).toBe(4)
  })

  it('supports _in filter with null', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'created_at', op: '_in', value: ['2024-01-15', null] }],
    })
    expect(result.filtered).toBe(2)
  })

  it('supports _nin filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'status', op: '_nin', value: ['deleted'] }],
    })
    expect(result.filtered).toBe(4)
    expect(result.data.every((r) => r.status !== 'deleted')).toBe(true)
  })

  it('supports _like filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'name', op: '_like', value: '%lp%' }],
    })
    expect(result.filtered).toBe(1)
    expect(result.data[0].name).toBe('Alpha')
  })

  it('supports _notlike filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'name', op: '_notlike', value: '%a%' }],
    })
    expect(result.filtered).toBe(1)
    expect(result.data[0].name).toBe('Epsilon')
  })

  it('supports _null filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'created_at', op: '_null', value: true }],
    })
    expect(result.filtered).toBe(1)
    expect(result.data[0].name).toBe('Delta')
  })

  it('supports _empty filter', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'status', op: '_empty', value: true }],
    })
    expect(result.filtered).toBe(1)
    expect(result.data[0].name).toBe('Epsilon')
  })

  it('supports _isvoid filter (null or empty)', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [{ field: 'status', op: '_isvoid', value: true }],
    })
    expect(result.filtered).toBe(1)
  })

  it('supports AND combination', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10, sort: [],
      conditions: [
        { field: 'status', op: '_eq', value: 'valid' },
        { field: 'price', op: '_gt', value: 15 },
      ],
    })
    expect(result.filtered).toBe(2)
  })

  it('supports sorting ASC', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10,
      conditions: [],
      sort: [{ field: 'price', dir: 'asc' }],
    })
    expect(result.data[0].price).toBe(5)
    expect(result.data[4].price).toBe(30)
  })

  it('supports sorting DESC', async () => {
    const result = await ds.query('t_order', {
      offset: 0, limit: 10,
      conditions: [],
      sort: [{ field: 'price', dir: 'desc' }],
    })
    expect(result.data[0].price).toBe(30)
    expect(result.data[4].price).toBe(5)
  })

  it('supports offset and limit', async () => {
    const result = await ds.query('t_order', {
      offset: 2, limit: 2, conditions: [], sort: [{ field: 'id', dir: 'asc' }],
    })
    expect(result.data).toHaveLength(2)
    expect(result.data[0].name).toBe('Gamma')
    expect(result.data[1].name).toBe('Delta')
  })

  it('supports count endpoint', async () => {
    const result = await ds.count('t_order', [
      { field: 'status', op: '_eq', value: 'valid' },
    ])
    expect(result.total).toBe(5)
    expect(result.filtered).toBe(3)
  })
})
