import { useQuery } from '@tanstack/react-query'
import { listarPedidos } from '../services/pedidoService'

export function usePedidos(params: Record<string, string | number | undefined>) {
  return useQuery({
    queryKey: ['pedidos', params],
    queryFn: () => listarPedidos(params),
  })
}
