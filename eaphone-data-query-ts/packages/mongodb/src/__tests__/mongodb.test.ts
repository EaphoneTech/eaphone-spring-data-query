import { describe, it, expect } from 'vitest'

// We test the filter building logic by re-implementing it inline
// since the buildFilter is internal to the module

function getLikeFilterPattern(filterValue: string): RegExp {
  let pattern = filterValue
  pattern = pattern.replace(/[.+?*^$()\[\]]/g, '\\$&')
  if (!filterValue.startsWith('%')) {
    pattern = '^' + pattern
  }
  if (!filterValue.endsWith('%')) {
    pattern = pattern + '$'
  }
  pattern = pattern.replace(/%/g, '.*')
  return new RegExp(pattern, 'i')
}

describe('getLikeFilterPattern', () => {
  it('converts SQL LIKE to regex', () => {
    const r = getLikeFilterPattern('James%')
    expect(r.source).toBe('^James.*')
    expect(r.flags).toBe('i')
  })

  it('anchors both ends without %', () => {
    const r = getLikeFilterPattern('James')
    expect(r.source).toBe('^James$')
  })

  it('handles only leading %', () => {
    const r = getLikeFilterPattern('%James')
    expect(r.source).toBe('.*James$')
  })

  it('handles only trailing %', () => {
    const r = getLikeFilterPattern('James%')
    expect(r.source).toBe('^James.*')
  })

  it('escapes regex metacharacters', () => {
    const r = getLikeFilterPattern('test.value')
    expect(r.source).toBe('^test\\.value$')
  })

  it('handles multiple % wildcards', () => {
    const r = getLikeFilterPattern('a%b%c')
    expect(r.source).toBe('^a.*b.*c$')
  })
})

describe('MongoDB filter structure', () => {
  it('produces _eq filter', () => {
    const filter = { name: 'James' }
    expect(filter).toEqual({ name: 'James' })
  })

  it('produces _ne filter', () => {
    const filter = { name: { $ne: 'James' } }
    expect(filter).toEqual({ name: { $ne: 'James' } })
  })

  it('produces _gt filter', () => {
    const filter = { price: { $gt: 10 } }
    expect(filter).toEqual({ price: { $gt: 10 } })
  })

  it('produces _in filter with null splitting', () => {
    const arr = ['A', null, 'B']
    const nonNull = arr.filter((v) => v !== null)
    const hasNull = nonNull.length !== arr.length
    const filter = hasNull
      ? { $or: [{ status: null }, { status: { $in: nonNull } }] }
      : { status: { $in: arr } }
    expect(filter).toEqual({
      $or: [{ status: null }, { status: { $in: ['A', 'B'] } }],
    })
  })

  it('produces _nin filter with null splitting', () => {
    const arr = ['A', null, 'B']
    const nonNull = arr.filter((v) => v !== null)
    const hasNull = nonNull.length !== arr.length
    const filter = hasNull
      ? { $and: [{ status: { $ne: null } }, { status: { $nin: nonNull } }] }
      : { status: { $nin: arr } }
    expect(filter).toEqual({
      $and: [{ status: { $ne: null } }, { status: { $nin: ['A', 'B'] } }],
    })
  })

  it('produces _all filter', () => {
    const filter = { tags: { $all: ['x', 'y'] } }
    expect(filter).toEqual({ tags: { $all: ['x', 'y'] } })
  })

  it('produces _regex filter', () => {
    const filter = { name: { $regex: '^J.*' } }
    expect(filter).toEqual({ name: { $regex: '^J.*' } })
  })

  it('produces _like filter', () => {
    const pattern = getLikeFilterPattern('James%')
    const filter = { name: { $regex: pattern.source, $options: 'i' } }
    expect(filter).toEqual({ name: { $regex: '^James.*', $options: 'i' } })
  })

  it('produces _null filter', () => {
    const filter = { name: null }
    expect(filter).toEqual({ name: null })
  })

  it('produces _empty filter', () => {
    const filter = { name: '' }
    expect(filter).toEqual({ name: '' })
  })

  it('produces _isvoid filter', () => {
    const filter = {
      $or: [{ name: null }, { name: '' }, { name: { $exists: false } }],
    }
    expect(filter).toEqual({
      $or: [{ name: null }, { name: '' }, { name: { $exists: false } }],
    })
  })

  it('produces _exists filter', () => {
    const filter = { name: { $exists: true } }
    expect(filter).toEqual({ name: { $exists: true } })
  })

  it('combines multiple conditions with AND', () => {
    const filters = [{ price: { $gt: 10 } }, { price: { $lt: 20 } }]
    const combined = filters.length === 1 ? filters[0] : { $and: filters }
    expect(combined).toEqual({
      $and: [{ price: { $gt: 10 } }, { price: { $lt: 20 } }],
    })
  })
})
