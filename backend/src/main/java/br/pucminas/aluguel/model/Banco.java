package br.pucminas.aluguel.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "bancos")
public class Banco extends Agente {

    protected Banco() {
    }

    public Banco(String login, String senha, String nome, String cnpj) {
        super(login, senha, nome, cnpj);
    }

    @Override
    public TipoUsuario getTipo() {
        return TipoUsuario.BANCO;
    }
}
