import { useState } from 'react'
import Modal from './Modal.jsx'

export default function Confirmacao({ titulo, mensagem, rotulo, aoConfirmar, aoFechar }) {
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')

  async function confirmar() {
    setEnviando(true)
    setErro('')
    try {
      await aoConfirmar()
    } catch (e) {
      setErro(e.message)
      setEnviando(false)
    }
  }

  return (
    <Modal titulo={titulo} aoFechar={aoFechar}>
      <p>{mensagem}</p>
      {erro && <p className="alerta" role="alert">{erro}</p>}
      <div className="acoes-form">
        <button type="button" className="botao botao-secundario" onClick={aoFechar}>Voltar</button>
        <button type="button" className="botao botao-perigo" disabled={enviando} onClick={confirmar}>
          {enviando ? 'Aguarde…' : rotulo}
        </button>
      </div>
    </Modal>
  )
}
