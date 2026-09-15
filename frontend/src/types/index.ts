export type StatusPedido =
  | 'CRIADO'
  | 'PROCESSANDO'
  | 'APROVADO'
  | 'REJEITADO'
  | 'CANCELADO'
  | 'FINALIZADO'

export interface Cliente {
  id: number
  nome: string
  cpf: string
  email: string
  dataCadastro: string
}

export interface Pedido {
  id: number
  descricao: string
  valor: number
  desconto: number
  valorFinal: number
  dataCriacao: string
  status: StatusPedido
  clienteId: number
  clienteNome: string
}

export interface HistoricoPedido {
  id: number
  pedidoId: number
  statusAnterior: StatusPedido | null
  statusNovo: StatusPedido
  dataHora: string
}

export interface Dashboard {
  totalClientes: number
  totalPedidos: number
  pedidosPorStatus: Record<string, number>
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ClienteFormData {
  nome: string
  cpf: string
  email: string
}

export interface PedidoFormData {
  descricao: string
  valor: number
  desconto: number
  clienteId: number
}
