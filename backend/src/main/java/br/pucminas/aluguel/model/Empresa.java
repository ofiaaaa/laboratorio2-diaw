package br.pucminas.aluguel.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresas")
public class Empresa extends Agente {

    protected Empresa() {
    }

    public Empresa(String login, String senha, String nome, String cnpj) {
        super(login, senha, nome, cnpj);
    }

    @Override
    public TipoUsuario getTipo() {
        return TipoUsuario.EMPRESA;
    }
}
