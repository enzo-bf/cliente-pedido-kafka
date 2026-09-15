interface PaginationProps {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  return (
    <div className="mt-4 flex items-center justify-between text-sm">
      <span className="text-slate-500">Página {page + 1} de {Math.max(totalPages, 1)}</span>
      <div className="flex gap-2">
        <button
          type="button"
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
          className="rounded-lg border px-3 py-1 disabled:opacity-40 dark:border-white/10"
        >
          Anterior
        </button>
        <button
          type="button"
          disabled={page + 1 >= totalPages}
          onClick={() => onPageChange(page + 1)}
          className="rounded-lg border px-3 py-1 disabled:opacity-40 dark:border-white/10"
        >
          Próxima
        </button>
      </div>
    </div>
  )
}
