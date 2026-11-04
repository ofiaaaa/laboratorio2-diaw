package br.pucminas.aluguel.dto;

import br.pucminas.aluguel.model.TipoUsuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ContratoRequest(
        @NotNull(message = "Informe o proprietário do automóvel")
        TipoUsuario tipoProprietario,

        @Valid
        Credito credito) {

    public record Credito(
            @NotNull(message = "Informe o valor do crédito")
            @Positive(message = "O valor deve ser positivo")
            @Digits(integer = 10, fraction = 2, message = "Valor inválido")
            BigDecimal valor,

            @NotNull(message = "Informe a taxa de juros")
            @PositiveOrZero(message = "A taxa não pode ser negativa")
            @DecimalMax(value = "100", message = "A taxa deve ser de no máximo 100%")
            @Digits(integer = 3, fraction = 2, message = "Taxa inválida")
            BigDecimal taxaJurosMensal,

            @NotNull(message = "Informe o número de parcelas")
            @Min(value = 1, message = "Informe ao menos 1 parcela")
            @Max(value = 120, message = "Informe no máximo 120 parcelas")
            Integer parcelas) {
    }
}
