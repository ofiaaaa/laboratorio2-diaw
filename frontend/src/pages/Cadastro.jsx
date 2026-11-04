import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import Campo from '../components/Campo.jsx'
import DadosClienteForm from '../components/DadosClienteForm.jsx'
import { rendimentosParaApi } from '../formato.js'

export default function Cadastro() {
  const { entrar } = useAuth()
  const [dados, setDados] = useState({
    login: '',
    senha: '',
    nome: '',
    rg: '',
    cpf: '',
    endereco: '',
    profissao: '',
    rendimentos: [],
  })
  const [erro, setErro] = useState('')
  const [erros, setErros] = useState({})
  const [enviando, setEnviando] = useState(false)

  function alterar(mudanca) {
    setDados((atuais) => ({ ...atuais, ...mudanca }))
  }

  async function enviar(evento) {
    evento.preventDefault()
    setEnviando(true)
    setErro('')
    setErros({})
    try {
      await api.post('/api/clientes', { ...dados, rendimentos: rendimentosParaApi(dados.rendimentos) })
      await entrar(dados.login, dados.senha)
    } catch (e) {
      setErro(e.message)
      setErros(e.campos ?? {})
      setEnviando(false)
    }
  }

  return (
    <main className="acesso acesso-cadastro">
      <form className="cartao acesso-form" onSubmit={enviar}>
        <h1>Criar conta de cliente</h1>
        {erro && <p className="alerta" role="alert">{erro}</p>}
        <div className="grade-2">
          <Campo
            rotulo="Usuário"
            autoComplete="username"
            required
            value={dados.login}
            erro={erros.login}
            onChange={(e) => alterar({ login: e.target.value })}
          />
          <Campo
            rotulo="Senha"
            type="password"
            autoComplete="new-password"
            minLength="8"
            maxLength="72"
            required
            dica="Mínimo de 8 caracteres"
            value={dados.senha}
            erro={erros.senha}
            onChange={(e) => alterar({ senha: e.target.value })}
          />
        </div>
        <DadosClienteForm dados={dados} alterar={alterar} erros={erros} />
        <button type="submit" className="botao botao-largo" disabled={enviando}>
          {enviando ? 'Criando conta…' : 'Criar conta'}
        </button>
        <p className="acesso-rodape">Já tem conta? <Link to="/login">Entrar</Link></p>
      </form>
    </main>
  )
}
