package br.pucminas.aluguel.dto;

import br.pucminas.aluguel.model.Cliente;
import br.pucminas.aluguel.model.PedidoAluguel;
import br.pucminas.aluguel.model.Rendimento;
import br.pucminas.aluguel.model.StatusPedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PedidoResponse(
        Long id,
        StatusPedido status,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal valorTotal,
        Long automovelId,
        String automovelDescricao,
        String automovelPlaca,
        Long clienteId,
        String clienteNome,
        String clienteProfissao,
        List<RendimentoDto> clienteRendimentos,
        BigDecimal clienteRendaTotal,
        String parecer,
        String agenteAvaliadorNome,
        ContratoResponse contrato,
        Instant criadoEm) {

    public static PedidoResponse de(PedidoAluguel pedido) {
        Cliente cliente = pedido.getCliente();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getDataInicio(),
                pedido.getDataFim(),
                pedido.getValorTotal(),
                pedido.getAutomovel().getId(),
                pedido.getAutomovel().getDescricao(),
                pedido.getAutomovel().getPlaca(),
                cliente.getId(),
                cliente.getNome(),
                cliente.getProfissao(),
                cliente.getRendimentos().stream().map(RendimentoDto::de).toList(),
                cliente.getRendimentos().stream().map(Rendimento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add),
                pedido.getParecer(),
                pedido.getAgenteAvaliador() == null ? null : pedido.getAgenteAvaliador().getNome(),
                pedido.getContrato() == null ? null : ContratoResponse.de(pedido.getContrato()),
                pedido.getCriadoEm());
    }
}
