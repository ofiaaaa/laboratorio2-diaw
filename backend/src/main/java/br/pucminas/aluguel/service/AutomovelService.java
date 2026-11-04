package br.pucminas.aluguel.service;

import br.pucminas.aluguel.dto.AutomovelRequest;
import br.pucminas.aluguel.dto.AutomovelResponse;
import br.pucminas.aluguel.exception.ApiException;
import br.pucminas.aluguel.model.Automovel;
import br.pucminas.aluguel.model.Usuario;
import br.pucminas.aluguel.repository.AutomovelRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutomovelService {

    private final AutomovelRepository automoveis;
    private final AuthService auth;

    public AutomovelService(AutomovelRepository automoveis, AuthService auth) {
        this.automoveis = automoveis;
        this.auth = auth;
    }

    /** Clientes veem apenas o que pode ser alugado; agentes veem toda a frota. */
    @Transactional(readOnly = true)
    public List<AutomovelResponse> listar(Long usuarioId) {
        List<Automovel> lista = auth.obterUsuario(usuarioId).isAgente()
                ? automoveis.findAllByOrderByMarcaAscModeloAsc()
                : automoveis.findDisponiveisParaAluguel();
        return lista.stream().map(AutomovelResponse::de).toList();
    }

    @Transactional
    public AutomovelResponse criar(Long agenteId, AutomovelRequest request) {
        Usuario agente = auth.obterUsuario(agenteId);
        String placa = request.placa().replace("-", "").toUpperCase(Locale.ROOT);
        String matricula = request.matricula().trim();
        if (automoveis.existsByPlaca(placa)) {
            throw ApiException.conflito("Já existe um automóvel com esta placa");
        }
        if (automoveis.existsByMatricula(matricula)) {
            throw ApiException.conflito("Já existe um automóvel com esta matrícula");
        }
        Automovel automovel = new Automovel(matricula, request.ano(), request.marca().trim(),
                request.modelo().trim(), placa, agente);
        return AutomovelResponse.de(automoveis.save(automovel));
    }
}
