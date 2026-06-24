import { describe, it, expect } from 'vitest'
import { parseOrderBy, parseFilter, parseQueryInput, splitNull } from '../query/parser.js'

describe('parseOrderBy', () => {
  it('returns empty for undefined', () => {
    expect(parseOrderBy(undefined)).toEqual([])
  })

  it('parses field+dir format', () => {
    const result = parseOrderBy([{ field: 'name', dir: 'asc' }])
    expect(result).toEqual([{ field: 'name', dir: 'asc' }])
  })

  it('parses shorthand map format', () => {
    const result = parseOrderBy([{ name: 'asc' }, { price: 'desc' }])
    expect(result).toEqual([
      { field: 'name', dir: 'asc' },
      { field: 'price', dir: 'desc' },
    ])
  })

  it('handles mixed formats', () => {
    const result = parseOrderBy([
      { field: 'id', dir: 'desc' },
      { name: 'asc' },
    ])
    expect(result).toEqual([
      { field: 'id', dir: 'desc' },
      { field: 'name', dir: 'asc' },
    ])
  })
})

describe('splitNull', () => {
  it('detects null in array', () => {
    expect(splitNull([1, null, 2])).toEqual({ hasNull: true, values: [1, 2] })
  })
  it('returns no null when absent', () => {
    expect(splitNull([1, 2, 3])).toEqual({ hasNull: false, values: [1, 2, 3] })
  })
})

describe('parseFilter', () => {
  it('handles _eq', () => {
    const result = parseFilter('name', { _eq: 'James' })
    expect(result).toEqual([{ field: 'name', op: '_eq', value: 'James' }])
  })

  it('handles _ne', () => {
    const result = parseFilter('name', { _ne: 'James' })
    expect(result).toEqual([{ field: 'name', op: '_ne', value: 'James' }])
  })

  it('handles _gt', () => {
    const result = parseFilter('price', { _gt: 10 })
    expect(result).toEqual([{ field: 'price', op: '_gt', value: 10 }])
  })

  it('handles _gte', () => {
    const result = parseFilter('price', { _gte: 10 })
    expect(result).toEqual([{ field: 'price', op: '_gte', value: 10 }])
  })

  it('handles _lt', () => {
    const result = parseFilter('price', { _lt: 10 })
    expect(result).toEqual([{ field: 'price', op: '_lt', value: 10 }])
  })

  it('handles _lte', () => {
    const result = parseFilter('price', { _lte: 10 })
    expect(result).toEqual([{ field: 'price', op: '_lte', value: 10 }])
  })

  it('handles _in', () => {
    const result = parseFilter('status', { _in: ['A', 'B'] })
    expect(result).toEqual([{ field: 'status', op: '_in', value: ['A', 'B'] }])
  })

  it('handles _nin', () => {
    const result = parseFilter('status', { _nin: ['A', 'B'] })
    expect(result).toEqual([{ field: 'status', op: '_nin', value: ['A', 'B'] }])
  })

  it('handles _all', () => {
    const result = parseFilter('tags', { _all: ['x', 'y'] })
    expect(result).toEqual([{ field: 'tags', op: '_all', value: ['x', 'y'] }])
  })

  it('handles _regex', () => {
    const result = parseFilter('name', { _regex: '^J.*' })
    expect(result).toEqual([{ field: 'name', op: '_regex', value: '^J.*' }])
  })

  it('handles _like', () => {
    const result = parseFilter('name', { _like: 'James%' })
    expect(result).toEqual([{ field: 'name', op: '_like', value: 'James%' }])
  })

  it('handles _notlike', () => {
    const result = parseFilter('name', { _notlike: 'James%' })
    expect(result).toEqual([{ field: 'name', op: '_notlike', value: 'James%' }])
  })

  it('handles _null', () => {
    const result = parseFilter('name', { _null: true })
    expect(result).toEqual([{ field: 'name', op: '_null', value: true }])
  })

  it('handles _empty', () => {
    const result = parseFilter('name', { _empty: true })
    expect(result).toEqual([{ field: 'name', op: '_empty', value: true }])
  })

  it('handles _isvoid', () => {
    const result = parseFilter('name', { _isvoid: true })
    expect(result).toEqual([{ field: 'name', op: '_isvoid', value: true }])
  })

  it('handles _exists', () => {
    const result = parseFilter('name', { _exists: true })
    expect(result).toEqual([{ field: 'name', op: '_exists', value: true }])
  })

  it('_eq takes priority over others', () => {
    const result = parseFilter('name', { _eq: 'James', _like: 'J%', _gt: 'K' })
    expect(result).toHaveLength(1)
    expect(result[0].op).toBe('_eq')
  })

  it('_ne takes priority after _eq', () => {
    const result = parseFilter('name', { _ne: 'James', _like: 'J%' })
    expect(result).toHaveLength(1)
    expect(result[0].op).toBe('_ne')
  })

  it('combines multiple operators when no priority conflict', () => {
    const result = parseFilter('price', { _gt: 10, _lt: 20 })
    expect(result).toHaveLength(2)
    expect(result.map((c) => c.op)).toEqual(['_gt', '_lt'])
  })

  it('applies type conversion with schema', () => {
    const result = parseFilter('price', { _gt: '10' }, { price: 'double' })
    expect(result[0].value).toBe(10)
  })

  it('applies type conversion with inline type', () => {
    const result = parseFilter('price', { type: 'double', _gt: '9.5', _lt: '10.5' })
    expect(result[0].value).toBe(9.5)
    expect(result[1].value).toBe(10.5)
  })

  it('handles _in with null splitting', () => {
    const result = parseFilter('status', { _in: ['A', null, 'B'] })
    expect(result[0].value).toEqual(['A', null, 'B'])
  })
})

describe('parseQueryInput', () => {
  it('uses defaults for missing fields', () => {
    const result = parseQueryInput({})
    expect(result.offset).toBe(0)
    expect(result.limit).toBe(10)
    expect(result.sort).toEqual([])
    expect(result.conditions).toEqual([])
  })

  it('parses where and order_by', () => {
    const result = parseQueryInput({
      offset: 5,
      limit: 20,
      order_by: [{ name: 'asc' }],
      where: { name: { _eq: 'Test' }, price: { _gt: 100 } },
    })
    expect(result.offset).toBe(5)
    expect(result.limit).toBe(20)
    expect(result.sort).toEqual([{ field: 'name', dir: 'asc' }])
    expect(result.conditions).toHaveLength(2)
  })

  it('preserves draw', () => {
    const result = parseQueryInput({ draw: 42 })
    expect(result.draw).toBe(42)
  })
})
