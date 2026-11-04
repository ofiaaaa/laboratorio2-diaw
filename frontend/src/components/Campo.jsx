import { useId } from 'react'

export default function Campo({ rotulo, erro, dica, children, ...props }) {
  const id = useId()
  const idErro = `${id}-erro`
  const atributos = {
    id,
    'aria-invalid': erro ? true : undefined,
    'aria-describedby': erro ? idErro : undefined,
    ...props,
  }

  return (
    <div className="campo">
      <label htmlFor={id}>{rotulo}</label>
      {children ? children(atributos) : <input {...atributos} />}
      {dica && !erro && <small className="dica">{dica}</small>}
      {erro && <small id={idErro} className="erro-campo">{erro}</small>}
    </div>
  )
}
