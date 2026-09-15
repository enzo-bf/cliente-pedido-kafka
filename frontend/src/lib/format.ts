export function formatCurrency(value: number) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value)
}

export function formatCpf(cpf: string) {
  const digits = cpf.replace(/\D/g, '')
  if (digits.length !== 11) {
    return cpf
  }
  return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
}

export function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}

export const STATUS_COLORS: Record<string, string> = {
  CRIADO: 'bg-sky-500/15 text-sky-300 border-sky-500/30',
  PROCESSANDO: 'bg-amber-500/15 text-amber-300 border-amber-500/30',
  APROVADO: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30',
  REJEITADO: 'bg-rose-500/15 text-rose-300 border-rose-500/30',
  CANCELADO: 'bg-slate-500/15 text-slate-300 border-slate-500/30',
  FINALIZADO: 'bg-violet-500/15 text-violet-300 border-violet-500/30',
}
