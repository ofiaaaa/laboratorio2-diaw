import { useCallback, useEffect, useState } from 'react'
import { api } from '../api.js'
import Campo from '../components/Campo.jsx'
import Modal from '../components/Modal.jsx'
import { TIPOS_USUARIO } from '../formato.js'

const VAZIO = { matricula: '', ano: '', marca: '', modelo: '', placa: '' }

function AutomovelForm({ aoSalvar, aoFechar }) {
  const [dados, setDados] = useState(VAZIO)
  const [erro, setErro] = useState('')
  const [erros, setErros] = useState({})
  const [enviando, setEnviando] = useState(false)

  function campo(nome) {
    return {
      value: dados[nome],
      erro: erros[nome],
      required: true,
      onChange: (evento) => setDados((atuais) => ({ ...atuais, [nome]: evento.target.value })),
    }
  }

  async function enviar(evento) {
    evento.preventDefault()
    setEnviando(true)
    setErro('')
    setErros({})
    try {
      await api.post('/api/automoveis', { ...dados, ano: Number(dados.ano) })
      aoSalvar()
    } catch (e) {
      setErro(e.message)
      setErros(e.campos ?? {})
      setEnviando(false)
    }
  }

  return (
    <Modal titulo="Cadastrar automóvel" aoFechar={aoFechar}>
      <form onSubmit={enviar}>
        {erro && <p className="alerta" role="alert">{erro}</p>}
        <div className="grade-2">
          <Campo rotulo="Marca" {...campo('marca')} />
          <Campo rotulo="Modelo" {...campo('modelo')} />
          <Campo rotulo="Ano" type="number" min="1950" max="2100" {...campo('ano')} />
          <Campo rotulo="Placa" dica="Exemplo: ABC1D23" {...campo('placa')} />
        </div>
        <Campo rotulo="Matrícula" {...campo('matricula')} />
        <div className="acoes-form">
          <button type="button" className="botao botao-secundario" onClick={aoFechar}>Voltar</button>
          <button type="submit" className="botao" disabled={enviando}>{enviando ? 'Salvando…' : 'Cadastrar'}</button>
        </div>
      </form>
    </Modal>
  )
}

export default function Automoveis() {
  const [automoveis, setAutomoveis] = useState(null)
  const [erro, setErro] = useState('')
  const [cadastrando, setCadastrando] = useState(false)

  const carregar = useCallback(() => {
    api.get('/api/automoveis').then(setAutomoveis).catch((e) => setErro(e.message))
  }, [])

  useEffect(carregar, [carregar])

  return (
    <>
      <div className="cabecalho-pagina">
        <h1>Automóveis</h1>
        <button type="button" className="botao" onClick={() => setCadastrando(true)}>Cadastrar automóvel</button>
      </div>
      {erro && <p className="alerta" role="alert">{erro}</p>}
      {automoveis === null && !erro && <p className="carregando">Carregando automóveis…</p>}
      {automoveis?.length === 0 && <div className="vazio"><p>Nenhum automóvel cadastrado.</p></div>}
      {automoveis?.length > 0 && (
        <div className="cartao tabela-container">
          <table>
            <thead>
              <tr>
                <th>Automóvel</th>
                <th>Ano</th>
                <th>Placa</th>
                <th>Matrícula</th>
                <th>Proprietário</th>
              </tr>
            </thead>
            <tbody>
              {automoveis.map((a) => (
                <tr key={a.id}>
                  <td>{a.marca} {a.modelo}</td>
                  <td>{a.ano}</td>
                  <td className="sem-quebra">{a.placa}</td>
                  <td className="sem-quebra">{a.matricula}</td>
                  <td>{a.proprietarioNome} <small className="dica">({TIPOS_USUARIO[a.proprietarioTipo]})</small></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {cadastrando && (
        <AutomovelForm
          aoSalvar={() => {
            setCadastrando(false)
            carregar()
          }}
          aoFechar={() => setCadastrando(false)}
        />
      )}
    </>
  )
}
