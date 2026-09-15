import { NavLink, Outlet } from 'react-router-dom'
import { LayoutDashboard, Users, ShoppingBag, Moon, Sun } from 'lucide-react'
import { useEffect, useState } from 'react'

const links = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/clientes', label: 'Clientes', icon: Users },
  { to: '/pedidos', label: 'Pedidos', icon: ShoppingBag },
]

export function AppLayout() {
  const [dark, setDark] = useState(true)

  useEffect(() => {
    document.documentElement.classList.toggle('dark', dark)
  }, [dark])

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900 dark:bg-slate-950 dark:text-slate-100">
      <div className="flex min-h-screen">
        <aside className="hidden w-64 shrink-0 border-r border-violet-500/20 bg-slate-950 p-6 text-white md:block">
          <p className="text-xs uppercase tracking-[0.3em] text-violet-300">GFT Lab</p>
          <h1 className="mt-2 text-2xl font-semibold">Cliente Pedido</h1>
          <nav className="mt-10 space-y-2">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.to === '/'}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-xl px-4 py-3 text-sm transition ${
                    isActive ? 'bg-violet-600 text-white' : 'text-slate-300 hover:bg-white/5'
                  }`
                }
              >
                <link.icon size={18} />
                {link.label}
              </NavLink>
            ))}
          </nav>
        </aside>
        <div className="flex min-w-0 flex-1 flex-col">
          <header className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-4 dark:border-white/10 dark:bg-slate-900 md:px-8">
            <nav className="flex gap-2 md:hidden">
              {links.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  className={({ isActive }) =>
                    `rounded-lg px-3 py-2 text-sm ${isActive ? 'bg-violet-600 text-white' : 'bg-slate-100 dark:bg-slate-800'}`
                  }
                >
                  {link.label}
                </NavLink>
              ))}
            </nav>
            <p className="hidden text-sm text-slate-500 dark:text-slate-400 md:block">
              Gestão corporativa de clientes, pedidos e eventos Kafka
            </p>
            <button
              type="button"
              onClick={() => setDark((value) => !value)}
              className="rounded-full border border-slate-200 p-2 dark:border-white/10"
            >
              {dark ? <Sun size={18} /> : <Moon size={18} />}
            </button>
          </header>
          <main className="flex-1 p-4 md:p-8">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  )
}
