import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { buscarCliente } from '../services/clienteService'
import { formatCpf, formatDateTime } from '../lib/format'
import { LoadingState } from '../components/ui/LoadingState'

export function ClienteDetalhePage() {
  const { id } = useParams()
  const { data, isLoading } = useQuery({
    queryKey: ['cliente', id],
    queryFn: () => buscarCliente(Number(id)),
  })

  if (isLoading || !data) {
    return <LoadingState />
  }

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <h2 className="text-3xl font-semibold">{data.nome}</h2>
      <div className="rounded-3xl border border-slate-200 bg-white p-6 dark:border-white/10 dark:bg-slate-900">
        <p><strong>CPF:</strong> {formatCpf(data.cpf)}</p>
        <p className="mt-2"><strong>E-mail:</strong> {data.email}</p>
        <p className="mt-2"><strong>Cadastro:</strong> {formatDateTime(data.dataCadastro)}</p>
      </div>
      <Link to="/clientes" className="text-violet-400">Voltar para listagem</Link>
    </div>
  )
}
