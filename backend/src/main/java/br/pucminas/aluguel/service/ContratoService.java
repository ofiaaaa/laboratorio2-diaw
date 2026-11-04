package br.pucminas.aluguel.service;

import br.pucminas.aluguel.dto.ContratoRequest;
import br.pucminas.aluguel.dto.PedidoResponse;
import br.pucminas.aluguel.exception.ApiException;
import br.pucminas.aluguel.model.Agente;
import br.pucminas.aluguel.model.Automovel;
import br.pucminas.aluguel.model.Banco;
import br.pucminas.aluguel.model.Contrato;
import br.pucminas.aluguel.model.ContratoCredito;
import br.pucminas.aluguel.model.PedidoAluguel;
import br.pucminas.aluguel.model.TipoUsuario;
import br.pucminas.aluguel.model.Usuario;
import br.pucminas.aluguel.repository.ContratoRepository;
import br.pucminas.aluguel.repository.PedidoAluguelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContratoService {

    private final ContratoRepository contratos;
    private final PedidoAluguelRepository pedidos;
    private final AuthService auth;

    public ContratoService(ContratoRepository contratos, PedidoAluguelRepository pedidos, AuthService auth) {
        this.contratos = contratos;
        this.pedidos = pedidos;
        this.auth = auth;
    }

    @Transactional
    public PedidoResponse executar(Long agenteId, Long pedidoId, ContratoRequest request) {
        if (!(auth.obterUsuario(agenteId) instanceof Agente agente)) {
            throw ApiException.proibido("Apenas agentes podem executar contratos");
        }
        PedidoAluguel pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> ApiException.naoEncontrado("Pedido não encontrado"));

        Usuario proprietario = definirProprietario(request.tipoProprietario(), agente, pedido);
        Contrato contrato = new Contrato(pedido, proprietario, agente, criarCredito(request.credito(), agente));
        pedido.registrarContrato(contrato);
        pedido.getAutomovel().setProprietario(proprietario);
        contratos.save(contrato);
        return PedidoResponse.de(pedido);
    }

    /**
     * O automóvel fica com o cliente do pedido ou com o próprio agente que executa o contrato,
     * desde que o tipo escolhido corresponda ao tipo do agente.
     */
    private Usuario definirProprietario(TipoUsuario tipo, Agente agente, PedidoAluguel pedido) {
        if (tipo == TipoUsuario.CLIENTE) {
            exigirSemOutrosPedidos(pedido);
            return pedido.getCliente();
        }
        if (tipo != agente.getTipo()) {
            throw ApiException.invalido("O automóvel só pode ser registrado no nome do próprio agente ou do cliente");
        }
        return agente;
    }

    private void exigirSemOutrosPedidos(PedidoAluguel pedido) {
        Automovel automovel = pedido.getAutomovel();
        if (pedidos.existsByAutomovelIdAndStatusInAndIdNot(automovel.getId(), PedidoAluguel.STATUS_ATIVOS, pedido.getId())) {
            throw ApiException.conflito("O automóvel tem outros pedidos ativos e não pode passar para o cliente");
        }
    }

    private ContratoCredito criarCredito(ContratoRequest.Credito credito, Agente agente) {
        if (credito == null) {
            return null;
        }
        if (!(agente instanceof Banco banco)) {
            throw ApiException.proibido("Apenas bancos podem conceder contrato de crédito");
        }
        return new ContratoCredito(banco, credito.valor(), credito.taxaJurosMensal(), credito.parcelas());
    }
}
