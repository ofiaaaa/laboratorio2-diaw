package br.pucminas.aluguel.dto;

import br.pucminas.aluguel.model.Automovel;
import br.pucminas.aluguel.model.TipoUsuario;

public record AutomovelResponse(
        Long id,
        String matricula,
        int ano,
        String marca,
        String modelo,
        String placa,
        String proprietarioNome,
        TipoUsuario proprietarioTipo) {

    public static AutomovelResponse de(Automovel automovel) {
        return new AutomovelResponse(
                automovel.getId(),
                automovel.getMatricula(),
                automovel.getAno(),
                automovel.getMarca(),
                automovel.getModelo(),
                automovel.getPlaca(),
                automovel.getProprietario().getNome(),
                automovel.getProprietario().getTipo());
    }
}
