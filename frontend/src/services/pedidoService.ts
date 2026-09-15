import { api } from './api'
import type { HistoricoPedido, Pedido, PedidoFormData, PageResponse, StatusPedido } from '../types'

export async function listarPedidos(params: Record<string, string | number | undefined>) {
  const { data } = await api.get<PageResponse<Pedido>>('/pedidos', { params })
  return data
}

export async function buscarPedido(id: number) {
  const { data } = await api.get<Pedido>(`/pedidos/${id}`)
  return data
}

export async function criarPedido(payload: PedidoFormData) {
  const { data } = await api.post<Pedido>('/pedidos', payload)
  return data
}

export async function atualizarPedido(id: number, payload: PedidoFormData) {
  const { data } = await api.put<Pedido>(`/pedidos/${id}`, payload)
  return data
}

export async function alterarStatusPedido(id: number, status: StatusPedido) {
  const { data } = await api.patch<Pedido>(`/pedidos/${id}/status`, { status })
  return data
}

export async function cancelarPedido(id: number) {
  const { data } = await api.post<Pedido>(`/pedidos/${id}/cancelar`)
  return data
}

export async function historicoPedido(id: number) {
  const { data } = await api.get<HistoricoPedido[]>(`/pedidos/${id}/historico`)
  return data
}
