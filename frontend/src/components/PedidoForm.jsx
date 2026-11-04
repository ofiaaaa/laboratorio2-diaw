import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { hojeIso } from '../formato.js'
import Campo from './Campo.jsx'
import Modal from './Modal.jsx'

export default function PedidoForm({ pedido, aoSalvar, aoFechar }) {
  const [automoveis, setAutomoveis] = useState(null)
  const [dados, setDados] = useState({
    automovelId: pedido?.automovelId ?? '',
    dataInicio: pedido?.dataInicio ?? '',
    dataFim: pedido?.dataFim ?? '',
    valorTotal: pedido?.valorTotal ?? '',
  })
  const [erro, setErro] = useState('')
  const [erros, setErros] = useState({})
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    api.get('/api/automoveis')
      .then((lista) => setAutomoveis(lista.filter((a) => a.proprietarioTipo !== 'CLIENTE')))
      .catch((e) => {
        setAutomoveis([])
        setErro(e.message)
      })
  }, [])

  function alterar(nome) {
    return (evento) => setDados((atuais) => ({ ...atuais, [nome]: evento.target.value }))
  }

  async function enviar(evento) {
    evento.preventDefault()
    setEnviando(true)
    setErro('')
    setErros({})
    const corpo = { ...dados, automovelId: Number(dados.automovelId), valorTotal: Number(dados.valorTotal) }
    try {
      const salvo = pedido
        ? await api.put(`/api/pedidos/${pedido.id}`, corpo)
        : await api.post('/api/pedidos', corpo)
      aoSalvar(salvo)
    } catch (e) {
      setErro(e.message)
      setErros(e.campos ?? {})
      setEnviando(false)
    }
  }

  return (
    <Modal titulo={pedido ? `Modificar pedido nº ${pedido.id}` : 'Novo pedido de aluguel'} aoFechar={aoFechar}>
      <form onSubmit={enviar}>
        {erro && <p className="alerta" role="alert">{erro}</p>}
        <Campo rotulo="Automóvel" erro={erros.automovelId} required value={dados.automovelId} onChange={alterar('automovelId')}>
          {(atributos) => (
            <select {...atributos} disabled={automoveis === null}>
              <option value="">{automoveis === null ? 'Carregando…' : 'Selecione'}</option>
              {automoveis?.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.marca} {a.modelo} ({a.ano}) · {a.placa}
                </option>
              ))}
            </select>
          )}
        </Campo>
        <div className="grade-2">
          <Campo
            rotulo="Início"
            type="date"
            min={hojeIso()}
            required
            value={dados.dataInicio}
            erro={erros.dataInicio}
            onChange={alterar('dataInicio')}
          />
          <Campo
            rotulo="Fim"
            type="date"
            min={dados.dataInicio || hojeIso()}
            required
            value={dados.dataFim}
            erro={erros.dataFim}
            onChange={alterar('dataFim')}
          />
        </div>
        <Campo
          rotulo="Valor total (R$)"
          type="number"
          min="0.01"
          step="0.01"
          required
          value={dados.valorTotal}
          erro={erros.valorTotal}
          onChange={alterar('valorTotal')}
        />
        <div className="acoes-form">
          <button type="button" className="botao botao-secundario" onClick={aoFechar}>Voltar</button>
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? 'Salvando…' : pedido ? 'Salvar alterações' : 'Enviar pedido'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
