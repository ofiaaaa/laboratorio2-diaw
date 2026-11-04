import Campo from './Campo.jsx'

const MAX_RENDIMENTOS = 3

/** Campos compartilhados entre o cadastro e a edição do cliente. */
export default function DadosClienteForm({ dados, alterar, erros }) {
  function campo(nome) {
    return {
      value: dados[nome],
      erro: erros[nome],
      onChange: (evento) => alterar({ [nome]: evento.target.value }),
    }
  }

  function alterarRendimento(indice, mudanca) {
    alterar({
      rendimentos: dados.rendimentos.map((r, i) => (i === indice ? { ...r, ...mudanca } : r)),
    })
  }

  return (
    <>
      <Campo rotulo="Nome completo" autoComplete="name" required {...campo('nome')} />
      <div className="grade-2">
        <Campo rotulo="RG" required {...campo('rg')} />
        <Campo rotulo="CPF" inputMode="numeric" required {...campo('cpf')} />
      </div>
      <Campo rotulo="Endereço" autoComplete="street-address" required {...campo('endereco')} />
      <Campo rotulo="Profissão" required {...campo('profissao')} />

      <fieldset className="rendimentos">
        <legend>Rendimentos <small>(até {MAX_RENDIMENTOS})</small></legend>
        {erros.rendimentos && <p className="erro-campo">{erros.rendimentos}</p>}
        {dados.rendimentos.map((rendimento, indice) => (
          <div className="rendimento" key={rendimento.chave}>
            <Campo
              rotulo="Entidade empregadora"
              required
              value={rendimento.entidadeEmpregadora}
              erro={erros[`rendimentos[${indice}].entidadeEmpregadora`]}
              onChange={(e) => alterarRendimento(indice, { entidadeEmpregadora: e.target.value })}
            />
            <Campo
              rotulo="Valor mensal (R$)"
              type="number"
              min="0.01"
              step="0.01"
              required
              value={rendimento.valor}
              erro={erros[`rendimentos[${indice}].valor`]}
              onChange={(e) => alterarRendimento(indice, { valor: e.target.value })}
            />
            <button
              type="button"
              className="botao botao-texto"
              onClick={() => alterar({ rendimentos: dados.rendimentos.filter((_, i) => i !== indice) })}
            >
              Remover
            </button>
          </div>
        ))}
        {dados.rendimentos.length < MAX_RENDIMENTOS && (
          <button
            type="button"
            className="botao botao-secundario"
            onClick={() => alterar({
              rendimentos: [...dados.rendimentos, { chave: crypto.randomUUID(), entidadeEmpregadora: '', valor: '' }],
            })}
          >
            Adicionar rendimento
          </button>
        )}
      </fieldset>
    </>
  )
}
