package br.pucminas.aluguel.dto;

import br.pucminas.aluguel.model.TipoUsuario;
import br.pucminas.aluguel.model.Usuario;

public record UsuarioResponse(Long id, String login, String nome, TipoUsuario tipo) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getLogin(), usuario.getNome(), usuario.getTipo());
    }
}
