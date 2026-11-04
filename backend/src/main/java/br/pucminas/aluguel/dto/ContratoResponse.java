package br.pucminas.aluguel.dto;

import br.pucminas.aluguel.model.Contrato;
import br.pucminas.aluguel.model.ContratoCredito;
import br.pucminas.aluguel.model.TipoUsuario;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoResponse(
        Long id,
        TipoUsuario tipoProprietario,
        String proprietarioNome,
        String agenteNome,
        LocalDate dataAssinatura,
        Credito credito) {

    public record Credito(String bancoNome, BigDecimal valor, BigDecimal taxaJurosMensal, int parcelas) {

        static Credito de(ContratoCredito credito) {
            return new Credito(credito.getBanco().getNome(), credito.getValor(),
                    credito.getTaxaJurosMensal(), credito.getParcelas());
        }
    }

    public static ContratoResponse de(Contrato contrato) {
        return new ContratoResponse(
                contrato.getId(),
                contrato.getTipoProprietario(),
                contrato.getProprietario().getNome(),
                contrato.getAgente().getNome(),
                contrato.getDataAssinatura(),
                contrato.getCredito() == null ? null : Credito.de(contrato.getCredito()));
    }
}
