package br.pucminas.aluguel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.validator.constraints.br.CPF;

public record ClienteUpdateRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 120, message = "Use no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "Informe o RG")
        @Size(max = 20, message = "Use no máximo 20 caracteres")
        String rg,

        @NotBlank(message = "Informe o CPF")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotBlank(message = "Informe o endereço")
        @Size(max = 200, message = "Use no máximo 200 caracteres")
        String endereco,

        @NotBlank(message = "Informe a profissão")
        @Size(max = 80, message = "Use no máximo 80 caracteres")
        String profissao,

        @NotNull(message = "Informe os rendimentos")
        @Size(max = 3, message = "Informe no máximo 3 rendimentos")
        List<@Valid @NotNull RendimentoDto> rendimentos,

        @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres")
        String novaSenha) {
}
