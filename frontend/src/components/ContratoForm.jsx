import { useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import { formatarMoeda } from '../formato.js'
import Campo from './Campo.jsx'
import Modal from './Modal.jsx'

export default function ContratoForm({ pedido, aoSalvar, aoFechar }) {
  const { usuario } = useAuth()
  const ehBanco = usuario.tipo === 'BANCO'
  const [tipoProprietario, setTipoProprietario] = useState(usuario.tipo)
  const [comCredito, setComCredito] = useState(false)
  const [credito, setCredito] = useState({ valor: pedido.valorTotal, taxaJurosMensal: '', parcelas: '' })
  const [erro, setErro] = useState('')
  const [erros, setErros] = useState({})
  const [enviando, setEnviando] = useState(false)

  function alterarCredito(nome) {
    return (evento) => setCredito((atual) => ({ ...atual, [nome]: evento.target.value }))
  }

  async function enviar(evento) {
    evento.preventDefault()
    setEnviando(true)
    setErro('')
    setErros({})
    const corpo = {
      tipoProprietario,
      credito: comCredito
        ? {
            valor: Number(credito.valor),
            taxaJurosMensal: Number(credito.taxaJurosMensal),
            parcelas: Number(credito.parcelas),
          }
        : null,
    }
    try {
      aoSalvar(await api.post(`/api/pedidos/${pedido.id}/contrato`, corpo))
    } catch (e) {
      setErro(e.message)
      setErros(e.campos ?? {})
      setEnviando(false)
    }
  }

  return (
    <Modal titulo={`Executar contrato do pedido nº ${pedido.id}`} aoFechar={aoFechar}>
      <form onSubmit={enviar}>
        {erro && <p className="alerta" role="alert">{erro}</p>}
        <p>
          {pedido.automovelDescricao} · {pedido.automovelPlaca}<br />
          <span className="dica">{pedido.clienteNome} · {formatarMoeda(pedido.valorTotal)}</span>
        </p>

        <fieldset className="opcoes">
          <legend>Registrar o automóvel em nome de</legend>
          <label>
            <input
              type="radio"
              name="proprietario"
              checked={tipoProprietario === usuario.tipo}
              onChange={() => setTipoProprietario(usuario.tipo)}
            />
            {usuario.nome} ({ehBanco ? 'banco' : 'empresa'})
          </label>
          <label>
            <input
              type="radio"
              name="proprietario"
              checked={tipoProprietario === 'CLIENTE'}
              onChange={() => setTipoProprietario('CLIENTE')}
            />
            {pedido.clienteNome} (cliente)
          </label>
        </fieldset>

        {ehBanco ? (
          <>
            <label className="checkbox">
              <input type="checkbox" checked={comCredito} onChange={(e) => setComCredito(e.target.checked)} />
              Conceder contrato de crédito
            </label>
            {comCredito && (
              <div className="grade-3">
                <Campo
                  rotulo="Valor (R$)"
                  type="number"
                  min="0.01"
                  step="0.01"
                  required
                  value={credito.valor}
                  erro={erros['credito.valor']}
                  onChange={alterarCredito('valor')}
                />
                <Campo
                  rotulo="Juros ao mês (%)"
                  type="number"
                  min="0"
                  max="100"
                  step="0.01"
                  required
                  value={credito.taxaJurosMensal}
                  erro={erros['credito.taxaJurosMensal']}
                  onChange={alterarCredito('taxaJurosMensal')}
                />
                <Campo
                  rotulo="Parcelas"
                  type="number"
                  min="1"
                  max="120"
                  step="1"
                  required
                  value={credito.parcelas}
                  erro={erros['credito.parcelas']}
                  onChange={alterarCredito('parcelas')}
                />
              </div>
            )}
          </>
        ) : (
          <p className="dica">Contratos de crédito são concedidos apenas por bancos.</p>
        )}

        <div className="acoes-form">
          <button type="button" className="botao botao-secundario" onClick={aoFechar}>Voltar</button>
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? 'Registrando…' : 'Executar contrato'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
