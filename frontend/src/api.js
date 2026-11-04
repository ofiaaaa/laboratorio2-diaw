const BASE_URL = import.meta.env.VITE_API_URL ?? ''
const CHAVE_TOKEN = 'aluguel.token'

const MENSAGENS_PADRAO = {
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Você não tem permissão para esta ação.',
  404: 'Registro não encontrado.',
}

export class ApiError extends Error {
  constructor(status, mensagem, campos = {}) {
    super(mensagem)
    this.status = status
    this.campos = campos
  }
}

let aoExpirarSessao = () => {}

export function definirAoExpirarSessao(callback) {
  aoExpirarSessao = callback
}

export function obterToken() {
  return sessionStorage.getItem(CHAVE_TOKEN)
}

export function salvarToken(token) {
  if (token) {
    sessionStorage.setItem(CHAVE_TOKEN, token)
  } else {
    sessionStorage.removeItem(CHAVE_TOKEN)
  }
}

async function requisitar(metodo, caminho, corpo) {
  const token = obterToken()
  const headers = {}
  if (corpo !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let resposta
  try {
    resposta = await fetch(`${BASE_URL}${caminho}`, {
      method: metodo,
      headers,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    })
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique se o back-end está em execução.')
  }

  if (resposta.ok) {
    return resposta.status === 204 ? null : resposta.json()
  }

  const erro = await resposta.json().catch(() => null)
  if (resposta.status === 401 && token) {
    aoExpirarSessao()
  }
  throw new ApiError(
    resposta.status,
    erro?.mensagem ?? MENSAGENS_PADRAO[resposta.status] ?? 'Algo deu errado. Tente novamente.',
    erro?.campos ?? {},
  )
}

export const api = {
  get: (caminho) => requisitar('GET', caminho),
  post: (caminho, corpo) => requisitar('POST', caminho, corpo),
  put: (caminho, corpo) => requisitar('PUT', caminho, corpo),
  delete: (caminho) => requisitar('DELETE', caminho),
}
