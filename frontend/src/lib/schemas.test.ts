import { describe, expect, it } from 'vitest'
import { clienteSchema } from './schemas'

describe('clienteSchema', () => {
  it('rejeita CPF inválido', () => {
    const result = clienteSchema.safeParse({
      nome: 'Ana Silva',
      cpf: '123',
      email: 'ana@email.com',
    })
    expect(result.success).toBe(false)
  })

  it('aceita dados válidos', () => {
    const result = clienteSchema.safeParse({
      nome: 'Ana Silva',
      cpf: '12345678901',
      email: 'ana@email.com',
    })
    expect(result.success).toBe(true)
  })
})
