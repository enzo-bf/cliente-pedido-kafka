import { useQuery } from '@tanstack/react-query'
import { obterDashboard } from '../services/dashboardService'

export function useDashboard() {
  return useQuery({
    queryKey: ['dashboard'],
    queryFn: obterDashboard,
  })
}
