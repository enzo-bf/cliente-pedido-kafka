import { useQuery } from '@tanstack/react-query'
import { listarClientes } from '../services/clienteService'

export function useClientes(params: Record<string, string | number | undefined>) {
  return useQuery({
    queryKey: ['clientes', params],
    queryFn: () => listarClientes(params),
  })
}
