import { useEffect, useRef } from 'react'

export default function Modal({ titulo, aoFechar, children }) {
  const dialogo = useRef(null)

  useEffect(() => {
    const elemento = dialogo.current
    if (!elemento.open) elemento.showModal()
    return () => elemento.close()
  }, [])

  return (
    <dialog
      ref={dialogo}
      className="modal"
      aria-label={titulo}
      onCancel={(evento) => {
        evento.preventDefault()
        aoFechar()
      }}
    >
      <div className="modal-topo">
        <h2>{titulo}</h2>
        <button type="button" className="fechar" aria-label="Fechar" onClick={aoFechar}>×</button>
      </div>
      {children}
    </dialog>
  )
}
