package br.pucminas.aluguel.service;

import br.pucminas.aluguel.dto.LoginRequest;
import br.pucminas.aluguel.dto.LoginResponse;
import br.pucminas.aluguel.dto.UsuarioResponse;
import br.pucminas.aluguel.exception.ApiException;
import br.pucminas.aluguel.model.Usuario;
import br.pucminas.aluguel.repository.UsuarioRepository;
import br.pucminas.aluguel.security.TokenService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByLogin(request.login().trim().toLowerCase(Locale.ROOT))
                .filter(encontrado -> passwordEncoder.matches(request.senha(), encontrado.getSenha()))
                .orElseThrow(() -> ApiException.naoAutenticado("Usuário ou senha inválidos"));
        return new LoginResponse(tokens.gerar(usuario), UsuarioResponse.de(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse usuarioAtual(Long usuarioId) {
        return UsuarioResponse.de(obterUsuario(usuarioId));
    }

    /** O token pode continuar válido depois de a conta ser excluída. */
    @Transactional(readOnly = true)
    public Usuario obterUsuario(Long usuarioId) {
        return usuarios.findById(usuarioId)
                .orElseThrow(() -> ApiException.naoAutenticado("Sessão inválida. Entre novamente"));
    }
}
