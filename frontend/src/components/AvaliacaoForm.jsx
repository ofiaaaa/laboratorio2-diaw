import { useState } from 'react'
import { api } from '../api.js'
import { formatarMoeda } from '../formato.js'
import Campo from './Campo.jsx'
import Modal from './Modal.jsx'

export default function AvaliacaoForm({ pedido, aoSalvar, aoFechar }) {
  const [parecer, setParecer] = useState('')
  const [erro, setErro] = useState('')
  const [erros, setErros] = useState({})
  const [enviando, setEnviando] = useState(false)

  async function avaliar(aprovado) {
    if (!parecer.trim()) {
      setErros({ parecer: 'Informe o parecer' })
      return
    }
    setEnviando(true)
    setErro('')
    setErros({})
    try {
      aoSalvar(await api.post(`/api/pedidos/${pedido.id}/avaliar`, { aprovado, parecer }))
    } catch (e) {
      setErro(e.message)
      setErros(e.campos ?? {})
      setEnviando(false)
    }
  }

  return (
    <Modal titulo={`Avaliar pedido nº ${pedido.id}`} aoFechar={aoFechar}>
      {erro && <p className="alerta" role="alert">{erro}</p>}
      <dl className="analise">
        <div><dt>Cliente</dt><dd>{pedido.clienteNome}</dd></div>
        <div><dt>Profissão</dt><dd>{pedido.clienteProfissao}</dd></div>
        <div><dt>Valor do pedido</dt><dd>{formatarMoeda(pedido.valorTotal)}</dd></div>
        <div><dt>Renda mensal</dt><dd>{formatarMoeda(pedido.clienteRendaTotal)}</dd></div>
      </dl>
      {pedido.clienteRendimentos.length > 0 ? (
        <ul className="lista-rendimentos">
          {pedido.clienteRendimentos.map((r, indice) => (
            <li key={indice}>
              {r.entidadeEmpregadora}: {formatarMoeda(r.valor)}
            </li>
          ))}
        </ul>
      ) : (
        <p className="dica">O cliente não informou rendimentos.</p>
      )}
      <Campo rotulo="Parecer financeiro" erro={erros.parecer} value={parecer} onChange={(e) => setParecer(e.target.value)}>
        {(atributos) => <textarea {...atributos} rows="3" maxLength="500" />}
      </Campo>
      <div className="acoes-form">
        <button type="button" className="botao botao-perigo" disabled={enviando} onClick={() => avaliar(false)}>
          Reprovar
        </button>
        <button type="button" className="botao" disabled={enviando} onClick={() => avaliar(true)}>
          Aprovar
        </button>
      </div>
    </Modal>
  )
}
