package br.pucminas.aluguel.dto;

public record LoginResponse(String token, UsuarioResponse usuario) {
}
