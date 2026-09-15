import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Plus } from 'lucide-react'
import { usePedidos } from '../hooks/usePedidos'
import { alterarStatusPedido, cancelarPedido } from '../services/pedidoService'
import { formatCurrency, formatDateTime } from '../lib/format'
import { LoadingState } from '../components/ui/LoadingState'
import { Pagination } from '../components/ui/Pagination'
import { StatusBadge } from '../components/ui/StatusBadge'
import { ConfirmDialog } from '../components/ui/ConfirmDialog'
import type { StatusPedido } from '../types'

const STATUS: StatusPedido[] = ['CRIADO', 'PROCESSANDO', 'APROVADO', 'REJEITADO', 'CANCELADO', 'FINALIZADO']

export function PedidosPage() {
  const [page, setPage] = useState(0)
  const [descricao, setDescricao] = useState('')
  const [status, setStatus] = useState('')
  const [cancelId, setCancelId] = useState<number | null>(null)
  const queryClient = useQueryClient()

  const params = useMemo(
    () => ({ descricao: descricao || undefined, status: status || undefined, page, size: 8 }),
    [descricao, status, page],
  )
  const { data, isLoading } = usePedidos(params)

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['pedidos'] })
    queryClient.invalidateQueries({ queryKey: ['dashboard'] })
  }

  const statusMutation = useMutation({
    mutationFn: ({ id, novoStatus }: { id: number; novoStatus: StatusPedido }) =>
      alterarStatusPedido(id, novoStatus),
    onSuccess: () => {
      toast.success('Status atualizado')
      invalidate()
    },
    onError: (error: Error) => toast.error(error.message),
  })

  const cancelMutation = useMutation({
    mutationFn: cancelarPedido,
    onSuccess: () => {
      toast.success('Pedido cancelado')
      invalidate()
      setCancelId(null)
    },
    onError: (error: Error) => toast.error(error.message),
  })

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-3xl font-semibold">Pedidos</h2>
          <p className="text-slate-500">Criação, edição, cancelamento e mudança de status</p>
        </div>
        <Link to="/pedidos/novo" className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-2 text-white">
          <Plus size={16} /> Novo pedido
        </Link>
      </div>
      <div className="grid gap-3 md:grid-cols-2">
        <input className="input" placeholder="Buscar descrição" value={descricao} onChange={(e) => { setDescricao(e.target.value); setPage(0) }} />
        <select className="input" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0) }}>
          <option value="">Todos os status</option>
          {STATUS.map((item) => (
            <option key={item} value={item}>{item}</option>
          ))}
        </select>
      </div>
      {isLoading || !data ? (
        <LoadingState />
      ) : (
        <div className="overflow-x-auto rounded-3xl border border-slate-200 bg-white dark:border-white/10 dark:bg-slate-900">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500 dark:bg-slate-800">
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">Cliente</th>
                <th className="px-4 py-3">Valor</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Data</th>
                <th className="px-4 py-3">Ações</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((pedido) => (
                <tr key={pedido.id} className="border-t border-slate-100 dark:border-white/5">
                  <td className="px-4 py-3">#{pedido.id}</td>
                  <td className="px-4 py-3">{pedido.clienteNome}</td>
                  <td className="px-4 py-3">{formatCurrency(pedido.valorFinal)}</td>
                  <td className="px-4 py-3"><StatusBadge status={pedido.status} /></td>
                  <td className="px-4 py-3">{formatDateTime(pedido.dataCriacao)}</td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap items-center gap-2">
                      <Link to={`/pedidos/${pedido.id}`} className="text-violet-400">Detalhes</Link>
                      <Link to={`/pedidos/${pedido.id}/editar`} className="text-sky-400">Editar</Link>
                      <select
                        className="rounded-lg border border-slate-200 bg-transparent px-2 py-1 dark:border-white/10"
                        defaultValue=""
                        onChange={(e) => {
                          if (e.target.value) {
                            statusMutation.mutate({ id: pedido.id, novoStatus: e.target.value as StatusPedido })
                            e.target.value = ''
                          }
                        }}
                      >
                        <option value="">Status</option>
                        {STATUS.map((item) => (
                          <option key={item} value={item}>{item}</option>
                        ))}
                      </select>
                      <button type="button" className="text-rose-400" onClick={() => setCancelId(pedido.id)}>
                        Cancelar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="px-4 pb-4">
            <Pagination page={data.number} totalPages={data.totalPages} onPageChange={setPage} />
          </div>
        </div>
      )}
      {cancelId && (
        <ConfirmDialog
          title="Cancelar pedido"
          message="O pedido será movido para CANCELADO, se a transição for permitida."
          confirmLabel="Cancelar pedido"
          onClose={() => setCancelId(null)}
          onConfirm={() => cancelMutation.mutate(cancelId)}
        />
      )}
    </div>
  )
}
