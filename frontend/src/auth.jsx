import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, definirAoExpirarSessao, obterToken, salvarToken } from './api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [carregando, setCarregando] = useState(() => Boolean(obterToken()))

  const sair = useCallback(() => {
    salvarToken(null)
    setUsuario(null)
  }, [])

  useEffect(() => {
    definirAoExpirarSessao(sair)
    if (!obterToken()) return
    api.get('/api/auth/me')
      .then(setUsuario)
      .catch(() => {})
      .finally(() => setCarregando(false))
  }, [sair])

  const entrar = useCallback(async (login, senha) => {
    const resposta = await api.post('/api/auth/login', { login, senha })
    salvarToken(resposta.token)
    setUsuario(resposta.usuario)
  }, [])

  const valor = useMemo(
    () => ({ usuario, carregando, entrar, sair, ehAgente: Boolean(usuario) && usuario.tipo !== 'CLIENTE' }),
    [usuario, carregando, entrar, sair],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
