package br.pucminas.aluguel.dto;

import br.pucminas.aluguel.model.Rendimento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RendimentoDto(
        @NotBlank(message = "Informe a entidade empregadora")
        @Size(max = 120, message = "Use no máximo 120 caracteres")
        String entidadeEmpregadora,

        @NotNull(message = "Informe o valor")
        @Positive(message = "O valor deve ser positivo")
        @Digits(integer = 10, fraction = 2, message = "Valor inválido")
        BigDecimal valor) {

    public static RendimentoDto de(Rendimento rendimento) {
        return new RendimentoDto(rendimento.getEntidadeEmpregadora(), rendimento.getValor());
    }

    public Rendimento paraModelo() {
        return new Rendimento(entidadeEmpregadora.trim(), valor);
    }
}
