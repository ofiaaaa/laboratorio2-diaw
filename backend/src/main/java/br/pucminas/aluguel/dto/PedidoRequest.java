package br.pucminas.aluguel.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PedidoRequest(
        @NotNull(message = "Selecione o automóvel")
        Long automovelId,

        @NotNull(message = "Informe a data de início")
        @FutureOrPresent(message = "A data de início não pode estar no passado")
        LocalDate dataInicio,

        @NotNull(message = "Informe a data de fim")
        LocalDate dataFim,

        @NotNull(message = "Informe o valor total")
        @Positive(message = "O valor deve ser positivo")
        @Digits(integer = 10, fraction = 2, message = "Valor inválido")
        BigDecimal valorTotal) {
}
