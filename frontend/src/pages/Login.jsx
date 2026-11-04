import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import Campo from '../components/Campo.jsx'

export default function Login() {
  const { entrar } = useAuth()
  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento) {
    evento.preventDefault()
    setEnviando(true)
    setErro('')
    try {
      await entrar(login, senha)
    } catch (e) {
      setErro(e.message)
      setEnviando(false)
    }
  }

  return (
    <main className="acesso">
      <div className="acesso-marca">
        <h1>Aluguel de Carros</h1>
        <p>Peça, acompanhe e contrate o aluguel do seu automóvel pela internet.</p>
      </div>
      <form className="cartao acesso-form" onSubmit={enviar}>
        <h2>Entrar</h2>
        {erro && <p className="alerta" role="alert">{erro}</p>}
        <Campo rotulo="Usuário" autoComplete="username" required value={login} onChange={(e) => setLogin(e.target.value)} />
        <Campo rotulo="Senha" type="password" autoComplete="current-password" required value={senha} onChange={(e) => setSenha(e.target.value)} />
        <button type="submit" className="botao botao-largo" disabled={enviando}>
          {enviando ? 'Entrando…' : 'Entrar'}
        </button>
        <p className="acesso-rodape">Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link></p>
        {import.meta.env.DEV && (
          <p className="dica">
            Perfil dev do back-end: <code>cliente</code>, <code>locadora</code> ou <code>banco</code>, com a senha <code>demo1234</code>.
          </p>
        )}
      </form>
    </main>
  )
}
