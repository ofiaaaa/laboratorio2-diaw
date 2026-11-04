import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { formatarCpf, formatarMoeda } from '../formato.js'

function rendaTotal(cliente) {
  return cliente.rendimentos.reduce((soma, r) => soma + Number(r.valor), 0)
}

export default function Clientes() {
  const [clientes, setClientes] = useState(null)
  const [erro, setErro] = useState('')

  useEffect(() => {
    api.get('/api/clientes').then(setClientes).catch((e) => setErro(e.message))
  }, [])

  return (
    <>
      <div className="cabecalho-pagina"><h1>Clientes</h1></div>
      {erro && <p className="alerta" role="alert">{erro}</p>}
      {clientes === null && !erro && <p className="carregando">Carregando clientes…</p>}
      {clientes?.length === 0 && <div className="vazio"><p>Nenhum cliente cadastrado.</p></div>}
      {clientes?.length > 0 && (
        <div className="cartao tabela-container">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>CPF</th>
                <th>RG</th>
                <th>Profissão</th>
                <th>Empregadores</th>
                <th className="numero">Renda mensal</th>
              </tr>
            </thead>
            <tbody>
              {clientes.map((cliente) => (
                <tr key={cliente.id}>
                  <td>{cliente.nome}</td>
                  <td className="sem-quebra">{formatarCpf(cliente.cpf)}</td>
                  <td className="sem-quebra">{cliente.rg}</td>
                  <td>{cliente.profissao}</td>
                  <td>{cliente.rendimentos.map((r) => r.entidadeEmpregadora).join(', ') || '—'}</td>
                  <td className="numero">{formatarMoeda(rendaTotal(cliente))}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
