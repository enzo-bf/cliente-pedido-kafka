import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { atualizarPedido, buscarPedido, criarPedido } from '../services/pedidoService'
import { listarClientes } from '../services/clienteService'
import { pedidoSchema } from '../lib/schemas'
import type { PedidoFormData } from '../types'

export function PedidoFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)

  const form = useForm<PedidoFormData>({
    resolver: zodResolver(pedidoSchema),
    mode: 'onChange',
    defaultValues: { descricao: '', valor: 0, desconto: 0, clienteId: 0 },
  })

  const clientes = useQuery({
    queryKey: ['clientes-select'],
    queryFn: () => listarClientes({ size: 200, page: 0 }),
  })

  const pedido = useQuery({
    queryKey: ['pedido', id],
    queryFn: () => buscarPedido(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (pedido.data) {
      form.reset({
        descricao: pedido.data.descricao,
        valor: pedido.data.valor,
        desconto: pedido.data.desconto,
        clienteId: pedido.data.clienteId,
      })
    }
  }, [pedido.data, form])

  const mutation = useMutation({
    mutationFn: (payload: PedidoFormData) =>
      isEdit ? atualizarPedido(Number(id), payload) : criarPedido(payload),
    onSuccess: () => {
      toast.success(isEdit ? 'Pedido atualizado' : 'Pedido criado')
      navigate('/pedidos')
    },
    onError: (error: Error) => toast.error(error.message),
  })

  return (
    <div className="mx-auto max-w-2xl">
      <h2 className="text-3xl font-semibold">{isEdit ? 'Editar pedido' : 'Novo pedido'}</h2>
      <form
        className="mt-6 space-y-4 rounded-3xl border border-slate-200 bg-white p-6 dark:border-white/10 dark:bg-slate-900"
        onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
      >
        <label className="block text-sm">
          <span className="mb-1 block text-slate-500">Cliente</span>
          <select className="input" {...form.register('clienteId', { valueAsNumber: true })}>
            <option value={0}>Selecione</option>
            {clientes.data?.content.map((cliente) => (
              <option key={cliente.id} value={cliente.id}>{cliente.nome}</option>
            ))}
          </select>
          {form.formState.errors.clienteId && (
            <span className="text-rose-500">{form.formState.errors.clienteId.message}</span>
          )}
        </label>
        <label className="block text-sm">
          <span className="mb-1 block text-slate-500">Descrição</span>
          <input className="input" {...form.register('descricao')} />
          {form.formState.errors.descricao && (
            <span className="text-rose-500">{form.formState.errors.descricao.message}</span>
          )}
        </label>
        <label className="block text-sm">
          <span className="mb-1 block text-slate-500">Valor</span>
          <input className="input" type="number" step="0.01" {...form.register('valor')} />
          {form.formState.errors.valor && (
            <span className="text-rose-500">{form.formState.errors.valor.message}</span>
          )}
        </label>
        <label className="block text-sm">
          <span className="mb-1 block text-slate-500">Desconto (%)</span>
          <input className="input" type="number" step="0.01" {...form.register('desconto')} />
          {form.formState.errors.desconto && (
            <span className="text-rose-500">{form.formState.errors.desconto.message}</span>
          )}
        </label>
        <div className="flex justify-end gap-3">
          <button type="button" onClick={() => navigate('/pedidos')} className="rounded-xl px-4 py-2">Voltar</button>
          <button
            type="submit"
            disabled={!form.formState.isValid || mutation.isPending}
            className="rounded-xl bg-violet-600 px-4 py-2 text-white disabled:opacity-50"
          >
            Salvar
          </button>
        </div>
      </form>
    </div>
  )
}
