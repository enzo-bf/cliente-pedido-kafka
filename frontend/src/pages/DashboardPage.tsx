import { useDashboard } from '../hooks/useDashboard'
import { LoadingState } from '../components/ui/LoadingState'
import { StatusBadge } from '../components/ui/StatusBadge'

export function DashboardPage() {
  const { data, isLoading, isError, error } = useDashboard()

  if (isLoading) {
    return <LoadingState label="Carregando métricas..." />
  }

  if (isError || !data) {
    return <p className="text-rose-400">{error?.message ?? 'Não foi possível carregar o dashboard'}</p>
  }

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-3xl font-semibold">Dashboard</h2>
        <p className="mt-1 text-slate-500">Visão executiva de clientes e pedidos</p>
      </div>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <MetricCard title="Total de clientes" value={data.totalClientes} accent="from-violet-600 to-fuchsia-500" />
        <MetricCard title="Total de pedidos" value={data.totalPedidos} accent="from-sky-600 to-cyan-400" />
        <MetricCard
          title="Pedidos em processamento"
          value={data.pedidosPorStatus.PROCESSANDO ?? 0}
          accent="from-amber-500 to-orange-400"
        />
      </div>
      <section className="rounded-3xl border border-slate-200 bg-white p-6 dark:border-white/10 dark:bg-slate-900">
        <h3 className="mb-4 text-lg font-medium">Pedidos por status</h3>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {Object.entries(data.pedidosPorStatus).map(([status, total]) => (
            <div key={status} className="flex items-center justify-between rounded-2xl bg-slate-50 px-4 py-3 dark:bg-slate-800">
              <StatusBadge status={status} />
              <strong>{total}</strong>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

function MetricCard({ title, value, accent }: { title: string; value: number; accent: string }) {
  return (
    <div className={`rounded-3xl bg-gradient-to-br ${accent} p-6 text-white shadow-lg shadow-violet-950/20`}>
      <p className="text-sm text-white/80">{title}</p>
      <p className="mt-3 text-4xl font-semibold">{value}</p>
    </div>
  )
}
