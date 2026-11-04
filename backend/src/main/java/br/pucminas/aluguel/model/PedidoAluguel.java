package br.pucminas.aluguel.model;

import br.pucminas.aluguel.exception.ApiException;
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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "pedidos_aluguel")
public class PedidoAluguel {

    /** Status em que o pedido ocupa o automóvel no período solicitado. */
    public static final List<StatusPedido> STATUS_ATIVOS =
            List.of(StatusPedido.EM_ANALISE, StatusPedido.APROVADO, StatusPedido.CONTRATADO);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "automovel_id")
    private Automovel automovel;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.EM_ANALISE;

    @Column(length = 500)
    private String parecer;

    @ManyToOne
    @JoinColumn(name = "agente_avaliador_id")
    private Agente agenteAvaliador;

    @OneToOne(mappedBy = "pedido")
    private Contrato contrato;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Version
    private long versao;

    protected PedidoAluguel() {
    }

    public PedidoAluguel(Cliente cliente, Automovel automovel, LocalDate dataInicio,
                         LocalDate dataFim, BigDecimal valorTotal) {
        this.cliente = cliente;
        this.automovel = automovel;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.valorTotal = valorTotal;
    }

    public void alterarPorCliente(Automovel automovel, LocalDate inicio, LocalDate fim, BigDecimal valor) {
        exigirStatus("O cliente só pode modificar pedidos em análise", StatusPedido.EM_ANALISE);
        aplicarAlteracao(automovel, inicio, fim, valor);
    }

    public void alterarPorAgente(Automovel automovel, LocalDate inicio, LocalDate fim, BigDecimal valor) {
        exigirStatus("O agente só pode modificar pedidos em análise ou aprovados",
                StatusPedido.EM_ANALISE, StatusPedido.APROVADO);
        aplicarAlteracao(automovel, inicio, fim, valor);
    }

    public void cancelar() {
        exigirStatus("Apenas pedidos em análise ou aprovados podem ser cancelados",
                StatusPedido.EM_ANALISE, StatusPedido.APROVADO);
        status = StatusPedido.CANCELADO;
    }

    public void avaliar(Agente agente, boolean aprovado, String parecer) {
        exigirStatus("Apenas pedidos em análise podem ser avaliados", StatusPedido.EM_ANALISE);
        this.agenteAvaliador = agente;
        this.parecer = parecer;
        this.status = aprovado ? StatusPedido.APROVADO : StatusPedido.REPROVADO;
    }

    public void registrarContrato(Contrato contrato) {
        exigirStatus("Apenas pedidos aprovados podem virar contrato", StatusPedido.APROVADO);
        this.contrato = contrato;
        this.status = StatusPedido.CONTRATADO;
    }

    private void aplicarAlteracao(Automovel automovel, LocalDate inicio, LocalDate fim, BigDecimal valor) {
        this.automovel = automovel;
        this.dataInicio = inicio;
        this.dataFim = fim;
        this.valorTotal = valor;
    }

    private void exigirStatus(String mensagem, StatusPedido... permitidos) {
        if (!List.of(permitidos).contains(status)) {
            throw ApiException.conflito(mensagem);
        }
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Automovel getAutomovel() {
        return automovel;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public String getParecer() {
        return parecer;
    }

    public Agente getAgenteAvaliador() {
        return agenteAvaliador;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
