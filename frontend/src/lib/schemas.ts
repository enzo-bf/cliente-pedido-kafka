import { z } from 'zod'

export const clienteSchema = z.object({
  nome: z.string().min(3, 'Informe o nome completo'),
  cpf: z.string().regex(/^\d{11}$/, 'CPF deve ter 11 dígitos'),
  email: z.string().email('E-mail inválido'),
})

export const pedidoSchema = z.object({
  descricao: z.string().min(3, 'Informe a descrição'),
  valor: z.coerce.number().positive('Valor deve ser maior que zero'),
  desconto: z.coerce.number().min(0, 'Desconto não pode ser negativo').max(20, 'Desconto máximo é 20%'),
  clienteId: z.coerce.number().int().positive('Selecione um cliente'),
})
