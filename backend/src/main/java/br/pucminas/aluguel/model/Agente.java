package br.pucminas.aluguel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "agentes")
public abstract class Agente extends Usuario {

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    protected Agente() {
    }

    protected Agente(String login, String senha, String nome, String cnpj) {
        super(login, senha, nome);
        this.cnpj = cnpj;
    }

    public String getCnpj() {
        return cnpj;
    }
}
