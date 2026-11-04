package br.pucminas.aluguel.service;

import br.pucminas.aluguel.dto.AvaliacaoRequest;
import br.pucminas.aluguel.dto.PedidoRequest;
import br.pucminas.aluguel.dto.PedidoResponse;
import br.pucminas.aluguel.exception.ApiException;
import br.pucminas.aluguel.model.Agente;
import br.pucminas.aluguel.model.Automovel;
import br.pucminas.aluguel.model.Cliente;
import br.pucminas.aluguel.model.PedidoAluguel;
import br.pucminas.aluguel.model.Usuario;
import br.pucminas.aluguel.repository.AutomovelRepository;
import br.pucminas.aluguel.repository.PedidoAluguelRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private static final Long PEDIDO_NOVO = -1L;

    private final PedidoAluguelRepository pedidos;
    private final AutomovelRepository automoveis;
    private final AuthService auth;

    public PedidoService(PedidoAluguelRepository pedidos, AutomovelRepository automoveis, AuthService auth) {
        this.pedidos = pedidos;
        this.automoveis = automoveis;
        this.auth = auth;
    }

    @Transactional
    public PedidoResponse criar(Long clienteId, PedidoRequest request) {
        if (!(auth.obterUsuario(clienteId) instanceof Cliente cliente)) {
            throw ApiException.proibido("Apenas clientes podem criar pedidos");
        }
        Automovel automovel = reservarAutomovel(request, PEDIDO_NOVO);
        PedidoAluguel pedido = new PedidoAluguel(cliente, automovel, request.dataInicio(), request.dataFim(),
                request.valorTotal());
        return PedidoResponse.de(pedidos.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Long usuarioId) {
        List<PedidoAluguel> lista = auth.obterUsuario(usuarioId).isAgente()
                ? pedidos.findAllByOrderByCriadoEmDesc()
                : pedidos.findByClienteIdOrderByCriadoEmDesc(usuarioId);
        return lista.stream().map(PedidoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obter(Long usuarioId, Long pedidoId) {
        return PedidoResponse.de(buscarVisivel(auth.obterUsuario(usuarioId), pedidoId));
    }

    @Transactional
    public PedidoResponse alterar(Long usuarioId, Long pedidoId, PedidoRequest request) {
        Usuario usuario = auth.obterUsuario(usuarioId);
        PedidoAluguel pedido = buscarVisivel(usuario, pedidoId);
        Automovel automovel = reservarAutomovel(request, pedidoId);
        if (usuario.isAgente()) {
            pedido.alterarPorAgente(automovel, request.dataInicio(), request.dataFim(), request.valorTotal());
        } else {
            pedido.alterarPorCliente(automovel, request.dataInicio(), request.dataFim(), request.valorTotal());
        }
        return PedidoResponse.de(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(Long clienteId, Long pedidoId) {
        PedidoAluguel pedido = buscarVisivel(auth.obterUsuario(clienteId), pedidoId);
        pedido.cancelar();
        return PedidoResponse.de(pedido);
    }

    @Transactional
    public PedidoResponse avaliar(Long agenteId, Long pedidoId, AvaliacaoRequest request) {
        if (!(auth.obterUsuario(agenteId) instanceof Agente agente)) {
            throw ApiException.proibido("Apenas agentes podem avaliar pedidos");
        }
        PedidoAluguel pedido = buscarVisivel(agente, pedidoId);
        pedido.avaliar(agente, request.aprovado(), request.parecer().trim());
        return PedidoResponse.de(pedido);
    }

    private PedidoAluguel buscarVisivel(Usuario usuario, Long pedidoId) {
        PedidoAluguel pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> ApiException.naoEncontrado("Pedido não encontrado"));
        if (!usuario.isAgente() && !pedido.getCliente().getId().equals(usuario.getId())) {
            throw ApiException.naoEncontrado("Pedido não encontrado");
        }
        return pedido;
    }

    private Automovel reservarAutomovel(PedidoRequest request, Long pedidoId) {
        if (!request.dataFim().isAfter(request.dataInicio())) {
            throw ApiException.invalido("A data de fim deve ser posterior à data de início");
        }
        Automovel automovel = automoveis.findByIdParaAtualizar(request.automovelId())
                .orElseThrow(() -> ApiException.naoEncontrado("Automóvel não encontrado"));
        if (!automovel.isDisponivelParaAluguel()) {
            throw ApiException.conflito("Este automóvel não está disponível para aluguel");
        }
        if (pedidos.existeConflitoDePeriodo(automovel.getId(), pedidoId, PedidoAluguel.STATUS_ATIVOS,
                request.dataInicio(), request.dataFim())) {
            throw ApiException.conflito("Este automóvel já tem um pedido ativo nesse período");
        }
        return automovel;
    }
}
