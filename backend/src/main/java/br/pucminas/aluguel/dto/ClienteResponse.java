package br.pucminas.aluguel.dto;

import br.pucminas.aluguel.model.Cliente;
import java.util.List;

public record ClienteResponse(
        Long id,
        String login,
        String nome,
        String rg,
        String cpf,
        String endereco,
        String profissao,
        List<RendimentoDto> rendimentos) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getLogin(),
                cliente.getNome(),
                cliente.getRg(),
                cliente.getCpf(),
                cliente.getEndereco(),
                cliente.getProfissao(),
                cliente.getRendimentos().stream().map(RendimentoDto::de).toList());
    }
}
