import { STATUS_COLORS } from '../../lib/format'

export function StatusBadge({ status }: { status: string }) {
  return (
    <span className={`rounded-full border px-3 py-1 text-xs font-medium ${STATUS_COLORS[status] ?? ''}`}>
      {status}
    </span>
  )
}
