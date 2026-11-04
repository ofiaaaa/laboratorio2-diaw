import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import { TIPOS_USUARIO } from '../formato.js'

export default function Layout() {
  const { usuario, ehAgente, sair } = useAuth()

  return (
    <>
      <header className="topo">
        <div className="topo-conteudo">
          <NavLink to="/pedidos" className="marca">Aluguel de Carros</NavLink>
          <nav className="menu" aria-label="Principal">
            <NavLink to="/pedidos">Pedidos</NavLink>
            {ehAgente ? (
              <>
                <NavLink to="/automoveis">Automóveis</NavLink>
                <NavLink to="/clientes">Clientes</NavLink>
              </>
            ) : (
              <NavLink to="/perfil">Meus dados</NavLink>
            )}
          </nav>
          <div className="sessao">
            <span className="sessao-nome">
              {usuario.nome} <small>{TIPOS_USUARIO[usuario.tipo]}</small>
            </span>
            <button type="button" className="botao botao-claro" onClick={sair}>Sair</button>
          </div>
        </div>
      </header>
      <main className="conteudo">
        <Outlet />
      </main>
    </>
  )
}
