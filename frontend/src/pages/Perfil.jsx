import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import Campo from '../components/Campo.jsx'
import Confirmacao from '../components/Confirmacao.jsx'
import DadosClienteForm from '../components/DadosClienteForm.jsx'
import { rendimentosParaApi } from '../formato.js'

function paraFormulario(cliente) {
  return {
    ...cliente,
    novaSenha: '',
    rendimentos: cliente.rendimentos.map((r) => ({ ...r, chave: crypto.randomUUID() })),
  }
}

export default function Perfil() {
  const { usuario, sair } = useAuth()
  const [dados, setDados] = useState(null)
  const [erro, setErro] = useState('')
  const [erros, setErros] = useState({})
  const [sucesso, setSucesso] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [confirmandoExclusao, setConfirmandoExclusao] = useState(false)

  useEffect(() => {
    api.get(`/api/clientes/${usuario.id}`)
      .then((cliente) => setDados(paraFormulario(cliente)))
      .catch((e) => setErro(e.message))
  }, [usuario.id])

  function alterar(mudanca) {
    setSucesso('')
    setDados((atuais) => ({ ...atuais, ...mudanca }))
  }

  async function enviar(evento) {
    evento.preventDefault()
    setEnviando(true)
    setErro('')
    setErros({})
    setSucesso('')
    try {
      const salvo = await api.put(`/api/clientes/${usuario.id}`, {
        nome: dados.nome,
        rg: dados.rg,
        cpf: dados.cpf,
        endereco: dados.endereco,
        profissao: dados.profissao,
        rendimentos: rendimentosParaApi(dados.rendimentos),
        novaSenha: dados.novaSenha || null,
      })
      setDados(paraFormulario(salvo))
      setSucesso('Dados atualizados.')
    } catch (e) {
      setErro(e.message)
      setErros(e.campos ?? {})
    } finally {
      setEnviando(false)
    }
  }

  return (
    <>
      <div className="cabecalho-pagina"><h1>Meus dados</h1></div>
      {erro && <p className="alerta" role="alert">{erro}</p>}
      {!dados && !erro && <p className="carregando">Carregando…</p>}
      {dados && (
        <form className="cartao formulario-pagina" onSubmit={enviar}>
          {sucesso && <p className="sucesso" role="status">{sucesso}</p>}
          <Campo rotulo="Usuário" value={dados.login} disabled />
          <DadosClienteForm dados={dados} alterar={alterar} erros={erros} />
          <Campo
            rotulo="Nova senha"
            type="password"
            autoComplete="new-password"
            minLength="8"
            maxLength="72"
            dica="Deixe em branco para manter a senha atual"
            value={dados.novaSenha}
            erro={erros.novaSenha}
            onChange={(e) => alterar({ novaSenha: e.target.value })}
          />
          <div className="acoes-form acoes-separadas">
            <button type="button" className="botao botao-texto-perigo" onClick={() => setConfirmandoExclusao(true)}>
              Excluir minha conta
            </button>
            <button type="submit" className="botao" disabled={enviando}>
              {enviando ? 'Salvando…' : 'Salvar alterações'}
            </button>
          </div>
        </form>
      )}
      {confirmandoExclusao && (
        <Confirmacao
          titulo="Excluir sua conta?"
          mensagem="Seus dados serão apagados e isso não pode ser desfeito. Contas com pedidos registrados não podem ser excluídas."
          rotulo="Excluir conta"
          aoConfirmar={async () => {
            await api.delete(`/api/clientes/${usuario.id}`)
            sair()
          }}
          aoFechar={() => setConfirmandoExclusao(false)}
        />
      )}
    </>
  )
}
