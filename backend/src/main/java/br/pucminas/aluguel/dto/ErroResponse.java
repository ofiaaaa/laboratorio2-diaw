package br.pucminas.aluguel.dto;

import java.util.Map;

public record ErroResponse(int status, String mensagem, Map<String, String> campos) {
}
