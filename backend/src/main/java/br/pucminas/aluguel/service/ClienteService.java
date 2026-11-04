package br.pucminas.aluguel.service;

import br.pucminas.aluguel.dto.ClienteCreateRequest;
import br.pucminas.aluguel.dto.ClienteResponse;
import br.pucminas.aluguel.dto.ClienteUpdateRequest;
import br.pucminas.aluguel.dto.RendimentoDto;
import br.pucminas.aluguel.exception.ApiException;
import br.pucminas.aluguel.model.Cliente;
import br.pucminas.aluguel.model.Rendimento;
import br.pucminas.aluguel.model.Usuario;
import br.pucminas.aluguel.repository.AutomovelRepository;
import br.pucminas.aluguel.repository.ClienteRepository;
import br.pucminas.aluguel.repository.PedidoAluguelRepository;
import br.pucminas.aluguel.repository.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;
    private final PedidoAluguelRepository pedidos;
    private final AutomovelRepository automoveis;
    private final AuthService auth;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clientes, UsuarioRepository usuarios, PedidoAluguelRepository pedidos,
                          AutomovelRepository automoveis, AuthService auth, PasswordEncoder passwordEncoder) {
        this.clientes = clientes;
        this.usuarios = usuarios;
        this.pedidos = pedidos;
        this.automoveis = automoveis;
        this.auth = auth;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ClienteResponse criar(ClienteCreateRequest request) {
        String login = request.login().trim().toLowerCase(Locale.ROOT);
        String cpf = somenteDigitos(request.cpf());
        String rg = request.rg().trim();
        if (usuarios.existsByLogin(login)) {
            throw ApiException.conflito("Este usuário já está em uso");
        }
        if (clientes.existsByCpf(cpf)) {
            throw ApiException.conflito("Já existe um cliente com este CPF");
        }
        if (clientes.existsByRg(rg)) {
            throw ApiException.conflito("Já existe um cliente com este RG");
        }
        Cliente cliente = new Cliente(login, passwordEncoder.encode(request.senha()), request.nome().trim(), rg, cpf,
                request.endereco().trim(), request.profissao().trim(), paraModelo(request.rendimentos()));
        return ClienteResponse.de(clientes.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clientes.findAllByOrderByNomeAsc().stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obter(Long usuarioId, Long clienteId) {
        Usuario usuario = auth.obterUsuario(usuarioId);
        if (!usuario.isAgente() && !usuario.getId().equals(clienteId)) {
            throw ApiException.proibido("Você só pode consultar os seus próprios dados");
        }
        return ClienteResponse.de(buscar(clienteId));
    }

    @Transactional
    public ClienteResponse atualizar(Long usuarioId, Long clienteId, ClienteUpdateRequest request) {
        Cliente cliente = buscarProprioCliente(usuarioId, clienteId);
        String cpf = somenteDigitos(request.cpf());
        String rg = request.rg().trim();
        if (clientes.existsByCpfAndIdNot(cpf, clienteId)) {
            throw ApiException.conflito("Já existe um cliente com este CPF");
        }
        if (clientes.existsByRgAndIdNot(rg, clienteId)) {
            throw ApiException.conflito("Já existe um cliente com este RG");
        }
        cliente.setNome(request.nome().trim());
        cliente.setRg(rg);
        cliente.setCpf(cpf);
        cliente.setEndereco(request.endereco().trim());
        cliente.setProfissao(request.profissao().trim());
        cliente.substituirRendimentos(paraModelo(request.rendimentos()));
        if (request.novaSenha() != null && !request.novaSenha().isEmpty()) {
            cliente.setSenha(passwordEncoder.encode(request.novaSenha()));
        }
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void excluir(Long usuarioId, Long clienteId) {
        Cliente cliente = buscarProprioCliente(usuarioId, clienteId);
        if (pedidos.existsByClienteId(clienteId) || automoveis.existsByProprietarioId(clienteId)) {
            throw ApiException.conflito("Não é possível excluir um cliente com pedidos ou automóveis registrados");
        }
        clientes.delete(cliente);
    }

    private Cliente buscarProprioCliente(Long usuarioId, Long clienteId) {
        if (!clienteId.equals(usuarioId)) {
            throw ApiException.proibido("Você só pode alterar os seus próprios dados");
        }
        return buscar(clienteId);
    }

    private Cliente buscar(Long clienteId) {
        return clientes.findById(clienteId)
                .orElseThrow(() -> ApiException.naoEncontrado("Cliente não encontrado"));
    }

    private List<Rendimento> paraModelo(List<RendimentoDto> rendimentos) {
        return rendimentos.stream().map(RendimentoDto::paraModelo).toList();
    }

    private String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
