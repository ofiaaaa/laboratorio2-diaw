import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth.jsx'
import Layout from './components/Layout.jsx'
import Automoveis from './pages/Automoveis.jsx'
import Cadastro from './pages/Cadastro.jsx'
import Clientes from './pages/Clientes.jsx'
import Login from './pages/Login.jsx'
import Pedidos from './pages/Pedidos.jsx'
import Perfil from './pages/Perfil.jsx'

function RotaPublica() {
  const { usuario, carregando } = useAuth()
  if (carregando) return <p className="carregando">Carregando…</p>
  return usuario ? <Navigate to="/pedidos" replace /> : <Outlet />
}

function RotaProtegida() {
  const { usuario, carregando } = useAuth()
  if (carregando) return <p className="carregando">Carregando…</p>
  return usuario ? <Layout /> : <Navigate to="/login" replace />
}

function SomenteAgente() {
  return useAuth().ehAgente ? <Outlet /> : <Navigate to="/pedidos" replace />
}

function SomenteCliente() {
  return useAuth().ehAgente ? <Navigate to="/pedidos" replace /> : <Outlet />
}

export default function App() {
  return (
    <Routes>
      <Route element={<RotaPublica />}>
        <Route path="/login" element={<Login />} />
        <Route path="/cadastro" element={<Cadastro />} />
      </Route>
      <Route element={<RotaProtegida />}>
        <Route path="/pedidos" element={<Pedidos />} />
        <Route element={<SomenteCliente />}>
          <Route path="/perfil" element={<Perfil />} />
        </Route>
        <Route element={<SomenteAgente />}>
          <Route path="/clientes" element={<Clientes />} />
          <Route path="/automoveis" element={<Automoveis />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/pedidos" replace />} />
    </Routes>
  )
}
