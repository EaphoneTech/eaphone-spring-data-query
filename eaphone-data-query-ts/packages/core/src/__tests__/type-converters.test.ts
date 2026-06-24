import { describe, it, expect } from 'vitest'
import { convertValue, parseDate } from '../type-converters.js'

describe('convertValue', () => {
  it('returns null for null/undefined', () => {
    expect(convertValue(null)).toBeNull()
    expect(convertValue(undefined)).toBeNull()
  })

  it('converts to string', () => {
    expect(convertValue(42, 'string')).toBe('42')
    expect(convertValue(true, 'string')).toBe('true')
  })

  it('converts to integer', () => {
    expect(convertValue('42', 'integer')).toBe(42)
    expect(convertValue(42.7, 'integer')).toBe(42)
    expect(convertValue('abc', 'integer')).toBeNull()
  })

  it('converts to double', () => {
    expect(convertValue('3.14', 'double')).toBe(3.14)
    expect(convertValue(42, 'double')).toBe(42)
    expect(convertValue('abc', 'double')).toBeNull()
  })

  it('converts to boolean', () => {
    expect(convertValue(true, 'boolean')).toBe(true)
    expect(convertValue(0, 'boolean')).toBe(false)
  })

  it('converts to date', () => {
    const d = convertValue('2024-01-15', 'date')
    expect(d).toBeInstanceOf(Date)
    expect((d as Date).toISOString()).toContain('2024-01-15')
  })

  it('returns raw value for unknown type', () => {
    expect(convertValue('hello')).toBe('hello')
    expect(convertValue(42)).toBe(42)
  })
})

describe('parseDate', () => {
  it('parses ISO date string', () => {
    const d = parseDate('2024-06-15T10:30:00Z')
    expect(d).toBeInstanceOf(Date)
    expect(d!.toISOString()).toBe('2024-06-15T10:30:00.000Z')
  })

  it('parses date-only string', () => {
    const d = parseDate('2024-01-01')
    expect(d).toBeInstanceOf(Date)
  })

  it('returns null for invalid date', () => {
    expect(parseDate('not-a-date')).toBeNull()
  })

  it('passes through Date object', () => {
    const orig = new Date('2024-06-15')
    expect(parseDate(orig)).toBe(orig)
  })
})
