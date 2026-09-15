import { useEffect, type ReactNode } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { atualizarCliente, buscarCliente, criarCliente } from '../services/clienteService'
import { clienteSchema } from '../lib/schemas'
import type { ClienteFormData } from '../types'

export function ClienteFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)

  const form = useForm<ClienteFormData>({
    resolver: zodResolver(clienteSchema),
    mode: 'onChange',
    defaultValues: { nome: '', cpf: '', email: '' },
  })

  const query = useQuery({
    queryKey: ['cliente', id],
    queryFn: () => buscarCliente(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (query.data) {
      form.reset({
        nome: query.data.nome,
        cpf: query.data.cpf,
        email: query.data.email,
      })
    }
  }, [query.data, form])

  const mutation = useMutation({
    mutationFn: (payload: ClienteFormData) =>
      isEdit ? atualizarCliente(Number(id), payload) : criarCliente(payload),
    onSuccess: () => {
      toast.success(isEdit ? 'Cliente atualizado' : 'Cliente cadastrado')
      navigate('/clientes')
    },
    onError: (error: Error) => toast.error(error.message),
  })

  return (
    <div className="mx-auto max-w-2xl">
      <h2 className="text-3xl font-semibold">{isEdit ? 'Editar cliente' : 'Novo cliente'}</h2>
      <form
        className="mt-6 space-y-4 rounded-3xl border border-slate-200 bg-white p-6 dark:border-white/10 dark:bg-slate-900"
        onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
      >
        <Field label="Nome" error={form.formState.errors.nome?.message}>
          <input className="input" {...form.register('nome')} />
        </Field>
        <Field label="CPF" error={form.formState.errors.cpf?.message}>
          <input className="input" maxLength={11} {...form.register('cpf')} />
        </Field>
        <Field label="E-mail" error={form.formState.errors.email?.message}>
          <input className="input" type="email" {...form.register('email')} />
        </Field>
        <div className="flex justify-end gap-3">
          <button type="button" onClick={() => navigate('/clientes')} className="rounded-xl px-4 py-2">
            Voltar
          </button>
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

function Field({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block text-slate-500">{label}</span>
      {children}
      {error && <span className="mt-1 block text-rose-500">{error}</span>}
    </label>
  )
}
