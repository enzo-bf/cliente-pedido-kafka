import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Pencil, Plus, Trash2, Eye } from 'lucide-react'
import { useClientes } from '../hooks/useClientes'
import { excluirCliente } from '../services/clienteService'
import { formatCpf, formatDateTime } from '../lib/format'
import { LoadingState } from '../components/ui/LoadingState'
import { Pagination } from '../components/ui/Pagination'
import { ConfirmDialog } from '../components/ui/ConfirmDialog'

export function ClientesPage() {
  const [page, setPage] = useState(0)
  const [nome, setNome] = useState('')
  const [cpf, setCpf] = useState('')
  const [email, setEmail] = useState('')
  const [deleteId, setDeleteId] = useState<number | null>(null)
  const queryClient = useQueryClient()

  const params = useMemo(
    () => ({ nome: nome || undefined, cpf: cpf || undefined, email: email || undefined, page, size: 8 }),
    [nome, cpf, email, page],
  )
  const { data, isLoading } = useClientes(params)

  const mutation = useMutation({
    mutationFn: excluirCliente,
    onSuccess: () => {
      toast.success('Cliente excluído')
      queryClient.invalidateQueries({ queryKey: ['clientes'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      setDeleteId(null)
    },
    onError: (error: Error) => toast.error(error.message),
  })

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-3xl font-semibold">Clientes</h2>
          <p className="text-slate-500">Busca instantânea por nome, CPF e e-mail</p>
        </div>
        <Link to="/clientes/novo" className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-2 text-white">
          <Plus size={16} /> Novo cliente
        </Link>
      </div>
      <div className="grid gap-3 md:grid-cols-3">
        <input className="input" placeholder="Pesquisar nome" value={nome} onChange={(e) => { setNome(e.target.value); setPage(0) }} />
        <input className="input" placeholder="Pesquisar CPF" value={cpf} onChange={(e) => { setCpf(e.target.value); setPage(0) }} />
        <input className="input" placeholder="Pesquisar e-mail" value={email} onChange={(e) => { setEmail(e.target.value); setPage(0) }} />
      </div>
      {isLoading || !data ? (
        <LoadingState />
      ) : (
        <div className="overflow-hidden rounded-3xl border border-slate-200 bg-white dark:border-white/10 dark:bg-slate-900">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500 dark:bg-slate-800">
              <tr>
                <th className="px-4 py-3">Nome</th>
                <th className="px-4 py-3">CPF</th>
                <th className="px-4 py-3">E-mail</th>
                <th className="px-4 py-3">Cadastro</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody>
              {data.content.map((cliente) => (
                <tr key={cliente.id} className="border-t border-slate-100 dark:border-white/5">
                  <td className="px-4 py-3 font-medium">{cliente.nome}</td>
                  <td className="px-4 py-3">{formatCpf(cliente.cpf)}</td>
                  <td className="px-4 py-3">{cliente.email}</td>
                  <td className="px-4 py-3">{formatDateTime(cliente.dataCadastro)}</td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      <Link to={`/clientes/${cliente.id}`} className="icon-btn"><Eye size={16} /></Link>
                      <Link to={`/clientes/${cliente.id}/editar`} className="icon-btn"><Pencil size={16} /></Link>
                      <button type="button" className="icon-btn text-rose-400" onClick={() => setDeleteId(cliente.id)}>
                        <Trash2 size={16} />
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
      {deleteId && (
        <ConfirmDialog
          title="Excluir cliente"
          message="Essa ação não poderá ser desfeita."
          confirmLabel="Excluir"
          onClose={() => setDeleteId(null)}
          onConfirm={() => mutation.mutate(deleteId)}
        />
      )}
    </div>
  )
}
