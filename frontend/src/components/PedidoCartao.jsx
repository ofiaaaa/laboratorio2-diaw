import { STATUS_PEDIDO, TIPOS_USUARIO, formatarData, formatarMoeda } from '../formato.js'

const ETAPAS = ['EM_ANALISE', 'APROVADO', 'CONTRATADO']

function Etapas({ status }) {
  const atual = ETAPAS.indexOf(status)
  if (atual === -1) return null
  return (
    <ol className="etapas" aria-label="Andamento do pedido">
      {ETAPAS.map((etapa, indice) => (
        <li
          key={etapa}
          className={indice <= atual ? 'etapa-feita' : undefined}
          aria-current={indice === atual ? 'step' : undefined}
        >
          {STATUS_PEDIDO[etapa]}
        </li>
      ))}
    </ol>
  )
}

export default function PedidoCartao({ pedido, mostrarCliente, children }) {
  const { contrato } = pedido

  return (
    <article className="cartao pedido">
      <div className="pedido-topo">
        <div>
          <h2>{pedido.automovelDescricao}</h2>
          <p className="dica">Pedido nº {pedido.id} · Placa {pedido.automovelPlaca}</p>
        </div>
        <span className={`selo selo-${pedido.status.toLowerCase()}`}>{STATUS_PEDIDO[pedido.status]}</span>
      </div>

      <Etapas status={pedido.status} />

      <dl className="analise">
        <div>
          <dt>Período</dt>
          <dd>{formatarData(pedido.dataInicio)} a {formatarData(pedido.dataFim)}</dd>
        </div>
        <div><dt>Valor total</dt><dd>{formatarMoeda(pedido.valorTotal)}</dd></div>
        {mostrarCliente && (
          <>
            <div><dt>Cliente</dt><dd>{pedido.clienteNome}</dd></div>
            <div><dt>Renda mensal</dt><dd>{formatarMoeda(pedido.clienteRendaTotal)}</dd></div>
          </>
        )}
      </dl>

      {pedido.parecer && (
        <p className="parecer">
          <strong>Parecer de {pedido.agenteAvaliadorNome}:</strong> {pedido.parecer}
        </p>
      )}

      {contrato && (
        <div className="contrato">
          <h3>Contrato nº {contrato.id}</h3>
          <p>
            Assinado em {formatarData(contrato.dataAssinatura)} por {contrato.agenteNome}.
            Automóvel registrado em nome de {contrato.proprietarioNome} ({TIPOS_USUARIO[contrato.tipoProprietario].toLowerCase()}).
          </p>
          {contrato.credito && (
            <p>
              Crédito de {formatarMoeda(contrato.credito.valor)} concedido por {contrato.credito.bancoNome},
              em {contrato.credito.parcelas}x, com juros de {Number(contrato.credito.taxaJurosMensal).toLocaleString('pt-BR')}% ao mês.
            </p>
          )}
        </div>
      )}

      {children && <div className="pedido-acoes">{children}</div>}
    </article>
  )
}
