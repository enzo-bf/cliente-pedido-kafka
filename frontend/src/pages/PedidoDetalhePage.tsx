import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { buscarPedido, historicoPedido } from '../services/pedidoService'
import { formatCurrency, formatDateTime } from '../lib/format'
import { LoadingState } from '../components/ui/LoadingState'
import { StatusBadge } from '../components/ui/StatusBadge'

export function PedidoDetalhePage() {
  const { id } = useParams()
  const pedido = useQuery({
    queryKey: ['pedido', id],
    queryFn: () => buscarPedido(Number(id)),
  })
  const historico = useQuery({
    queryKey: ['pedido-historico', id],
    queryFn: () => historicoPedido(Number(id)),
  })

  if (pedido.isLoading || !pedido.data) {
    return <LoadingState />
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-semibold">Pedido #{pedido.data.id}</h2>
        <p className="text-slate-500">{pedido.data.descricao}</p>
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <div className="rounded-3xl border border-slate-200 bg-white p-6 dark:border-white/10 dark:bg-slate-900">
          <p><strong>Cliente:</strong> {pedido.data.clienteNome}</p>
          <p className="mt-2"><strong>Valor:</strong> {formatCurrency(pedido.data.valor)}</p>
          <p className="mt-2"><strong>Desconto:</strong> {pedido.data.desconto}%</p>
          <p className="mt-2"><strong>Valor final:</strong> {formatCurrency(pedido.data.valorFinal)}</p>
          <p className="mt-2"><strong>Data:</strong> {formatDateTime(pedido.data.dataCriacao)}</p>
          <p className="mt-2"><StatusBadge status={pedido.data.status} /></p>
        </div>
        <div className="rounded-3xl border border-slate-200 bg-white p-6 dark:border-white/10 dark:bg-slate-900">
          <h3 className="mb-4 font-medium">Histórico</h3>
          <div className="space-y-3">
            {historico.data?.map((item) => (
              <div key={item.id} className="rounded-xl bg-slate-50 p-3 text-sm dark:bg-slate-800">
                <p>{item.statusAnterior ?? '—'} → {item.statusNovo}</p>
                <p className="text-slate-500">{formatDateTime(item.dataHora)}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
      <Link to="/pedidos" className="text-violet-400">Voltar para listagem</Link>
    </div>
  )
}
