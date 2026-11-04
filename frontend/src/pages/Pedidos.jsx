import { useCallback, useEffect, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import AvaliacaoForm from '../components/AvaliacaoForm.jsx'
import Confirmacao from '../components/Confirmacao.jsx'
import ContratoForm from '../components/ContratoForm.jsx'
import PedidoCartao from '../components/PedidoCartao.jsx'
import PedidoForm from '../components/PedidoForm.jsx'
import { STATUS_PEDIDO } from '../formato.js'

export default function Pedidos() {
  const { ehAgente } = useAuth()
  const [pedidos, setPedidos] = useState(null)
  const [erro, setErro] = useState('')
  const [filtro, setFiltro] = useState('TODOS')
  // { tipo: 'novo' | 'modificar' | 'cancelar' | 'avaliar' | 'contrato', pedido }
  const [acao, setAcao] = useState(null)

  const carregar = useCallback(() => {
    api.get('/api/pedidos')
      .then((lista) => {
        setPedidos(lista)
        setErro('')
      })
      .catch((e) => setErro(e.message))
  }, [])

  useEffect(carregar, [carregar])

  function concluir() {
    setAcao(null)
    carregar()
  }

  const fechar = () => setAcao(null)
  const visiveis = pedidos?.filter((p) => filtro === 'TODOS' || p.status === filtro)

  return (
    <>
      <div className="cabecalho-pagina">
        <h1>{ehAgente ? 'Pedidos de aluguel' : 'Meus pedidos'}</h1>
        {!ehAgente && (
          <button type="button" className="botao" onClick={() => setAcao({ tipo: 'novo' })}>Novo pedido</button>
        )}
      </div>

      {erro && <p className="alerta" role="alert">{erro}</p>}

      {pedidos?.length > 0 && (
        <div className="filtros" role="group" aria-label="Filtrar por status">
          {[['TODOS', 'Todos'], ...Object.entries(STATUS_PEDIDO)].map(([valor, rotulo]) => (
            <button
              key={valor}
              type="button"
              className="filtro"
              aria-pressed={filtro === valor}
              onClick={() => setFiltro(valor)}
            >
              {rotulo}
            </button>
          ))}
        </div>
      )}

      {pedidos === null && !erro && <p className="carregando">Carregando pedidos…</p>}

      {pedidos?.length === 0 && (
        <div className="vazio">
          <p>{ehAgente ? 'Nenhum pedido foi enviado ainda.' : 'Você ainda não fez nenhum pedido.'}</p>
        </div>
      )}

      {visiveis?.length === 0 && pedidos.length > 0 && (
        <div className="vazio"><p>Nenhum pedido com este status.</p></div>
      )}

      <div className="lista">
        {visiveis?.map((pedido) => {
          const emAnalise = pedido.status === 'EM_ANALISE'
          const aprovado = pedido.status === 'APROVADO'
          const abrir = (tipo) => () => setAcao({ tipo, pedido })
          return (
            <PedidoCartao key={pedido.id} pedido={pedido} mostrarCliente={ehAgente}>
              {ehAgente ? (
                <>
                  {emAnalise && <button type="button" className="botao" onClick={abrir('avaliar')}>Avaliar</button>}
                  {aprovado && <button type="button" className="botao" onClick={abrir('contrato')}>Executar contrato</button>}
                  {(emAnalise || aprovado) && (
                    <button type="button" className="botao botao-secundario" onClick={abrir('modificar')}>Modificar</button>
                  )}
                </>
              ) : (
                <>
                  {emAnalise && (
                    <button type="button" className="botao botao-secundario" onClick={abrir('modificar')}>Modificar</button>
                  )}
                  {(emAnalise || aprovado) && (
                    <button type="button" className="botao botao-texto" onClick={abrir('cancelar')}>Cancelar pedido</button>
                  )}
                </>
              )}
            </PedidoCartao>
          )
        })}
      </div>

      {(acao?.tipo === 'novo' || acao?.tipo === 'modificar') && (
        <PedidoForm pedido={acao.pedido} aoSalvar={concluir} aoFechar={fechar} />
      )}
      {acao?.tipo === 'avaliar' && <AvaliacaoForm pedido={acao.pedido} aoSalvar={concluir} aoFechar={fechar} />}
      {acao?.tipo === 'contrato' && <ContratoForm pedido={acao.pedido} aoSalvar={concluir} aoFechar={fechar} />}
      {acao?.tipo === 'cancelar' && (
        <Confirmacao
          titulo={`Cancelar pedido nº ${acao.pedido.id}?`}
          mensagem={`O pedido de ${acao.pedido.automovelDescricao} será cancelado e o período ficará livre. Isso não pode ser desfeito.`}
          rotulo="Cancelar pedido"
          aoConfirmar={async () => {
            await api.post(`/api/pedidos/${acao.pedido.id}/cancelar`)
            concluir()
          }}
          aoFechar={fechar}
        />
      )}
    </>
  )
}
