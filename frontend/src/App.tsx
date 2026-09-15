import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/layout/AppLayout'
import { DashboardPage } from './pages/DashboardPage'
import { ClientesPage } from './pages/ClientesPage'
import { ClienteFormPage } from './pages/ClienteFormPage'
import { ClienteDetalhePage } from './pages/ClienteDetalhePage'
import { PedidosPage } from './pages/PedidosPage'
import { PedidoFormPage } from './pages/PedidoFormPage'
import { PedidoDetalhePage } from './pages/PedidoDetalhePage'

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/clientes" element={<ClientesPage />} />
        <Route path="/clientes/novo" element={<ClienteFormPage />} />
        <Route path="/clientes/:id" element={<ClienteDetalhePage />} />
        <Route path="/clientes/:id/editar" element={<ClienteFormPage />} />
        <Route path="/pedidos" element={<PedidosPage />} />
        <Route path="/pedidos/novo" element={<PedidoFormPage />} />
        <Route path="/pedidos/:id" element={<PedidoDetalhePage />} />
        <Route path="/pedidos/:id/editar" element={<PedidoFormPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
