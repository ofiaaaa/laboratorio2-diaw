package br.pucminas.aluguel.controller;

import br.pucminas.aluguel.dto.AvaliacaoRequest;
import br.pucminas.aluguel.dto.ContratoRequest;
import br.pucminas.aluguel.dto.PedidoRequest;
import br.pucminas.aluguel.dto.PedidoResponse;
import br.pucminas.aluguel.service.ContratoService;
import br.pucminas.aluguel.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidos;
    private final ContratoService contratos;

    public PedidoController(PedidoService pedidos, ContratoService contratos) {
        this.pedidos = pedidos;
        this.contratos = contratos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PedidoRequest request) {
        return pedidos.criar(Long.valueOf(jwt.getSubject()), request);
    }

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return pedidos.listar(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public PedidoResponse obter(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return pedidos.obter(Long.valueOf(jwt.getSubject()), id);
    }

    @PutMapping("/{id}")
    public PedidoResponse alterar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                  @Valid @RequestBody PedidoRequest request) {
        return pedidos.alterar(Long.valueOf(jwt.getSubject()), id, request);
    }

    @PostMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return pedidos.cancelar(Long.valueOf(jwt.getSubject()), id);
    }

    @PostMapping("/{id}/avaliar")
    public PedidoResponse avaliar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                  @Valid @RequestBody AvaliacaoRequest request) {
        return pedidos.avaliar(Long.valueOf(jwt.getSubject()), id, request);
    }

    @PostMapping("/{id}/contrato")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse executarContrato(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                           @Valid @RequestBody ContratoRequest request) {
        return contratos.executar(Long.valueOf(jwt.getSubject()), id, request);
    }
}
