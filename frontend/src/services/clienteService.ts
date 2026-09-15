import { api } from './api'
import type { Cliente, ClienteFormData, PageResponse } from '../types'

export async function listarClientes(params: Record<string, string | number | undefined>) {
  const { data } = await api.get<PageResponse<Cliente>>('/clientes', { params })
  return data
}

export async function buscarCliente(id: number) {
  const { data } = await api.get<Cliente>(`/clientes/${id}`)
  return data
}

export async function criarCliente(payload: ClienteFormData) {
  const { data } = await api.post<Cliente>('/clientes', payload)
  return data
}

export async function atualizarCliente(id: number, payload: ClienteFormData) {
  const { data } = await api.put<Cliente>(`/clientes/${id}`, payload)
  return data
}

export async function excluirCliente(id: number) {
  await api.delete(`/clientes/${id}`)
}
