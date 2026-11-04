package br.pucminas.aluguel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
        @NotNull(message = "Informe se o parecer é positivo")
        Boolean aprovado,

        @NotBlank(message = "Informe o parecer")
        @Size(max = 500, message = "Use no máximo 500 caracteres")
        String parecer) {
}
