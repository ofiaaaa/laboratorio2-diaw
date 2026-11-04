package br.pucminas.aluguel.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", unique = true)
    private PedidoAluguel pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_proprietario", nullable = false, length = 20)
    private TipoUsuario tipoProprietario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proprietario_id")
    private Usuario proprietario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agente_id")
    private Agente agente;

    @Column(name = "data_assinatura", nullable = false)
    private LocalDate dataAssinatura;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "contrato_credito_id")
    private ContratoCredito credito;

    protected Contrato() {
    }

    public Contrato(PedidoAluguel pedido, Usuario proprietario, Agente agente, ContratoCredito credito) {
        this.pedido = pedido;
        this.proprietario = proprietario;
        this.tipoProprietario = proprietario.getTipo();
        this.agente = agente;
        this.credito = credito;
        this.dataAssinatura = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public PedidoAluguel getPedido() {
        return pedido;
    }

    public TipoUsuario getTipoProprietario() {
        return tipoProprietario;
    }

    public Usuario getProprietario() {
        return proprietario;
    }

    public Agente getAgente() {
        return agente;
    }

    public LocalDate getDataAssinatura() {
        return dataAssinatura;
    }

    public ContratoCredito getCredito() {
        return credito;
    }
}
