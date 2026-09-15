import { describe, expect, it } from 'vitest'
import { formatCpf, formatCurrency } from '../lib/format'

describe('format', () => {
  it('formata CPF', () => {
    expect(formatCpf('12345678901')).toBe('123.456.789-01')
  })

  it('formata moeda BRL', () => {
    expect(formatCurrency(1500)).toContain('1.500')
  })
})
