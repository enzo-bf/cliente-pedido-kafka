export function LoadingState({ label = 'Carregando...' }: { label?: string }) {
  return (
    <div className="flex items-center justify-center rounded-2xl border border-slate-200 bg-white p-10 dark:border-white/10 dark:bg-slate-900">
      <div className="h-6 w-6 animate-spin rounded-full border-2 border-violet-500 border-t-transparent" />
      <span className="ml-3 text-sm text-slate-500">{label}</span>
    </div>
  )
}
