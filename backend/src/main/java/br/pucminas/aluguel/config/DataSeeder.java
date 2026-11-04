package br.pucminas.aluguel.config;

import br.pucminas.aluguel.model.Automovel;
import br.pucminas.aluguel.model.Banco;
import br.pucminas.aluguel.model.Cliente;
import br.pucminas.aluguel.model.Empresa;
import br.pucminas.aluguel.model.Rendimento;
import br.pucminas.aluguel.repository.AutomovelRepository;
import br.pucminas.aluguel.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Agentes não se cadastram sozinhos: entram no sistema por esta carga inicial. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarios;
    private final AutomovelRepository automoveis;
    private final PasswordEncoder passwordEncoder;
    private final String senha;

    public DataSeeder(UsuarioRepository usuarios, AutomovelRepository automoveis, PasswordEncoder passwordEncoder,
                      @Value("${app.seed.senha}") String senha) {
        this.usuarios = usuarios;
        this.automoveis = automoveis;
        this.passwordEncoder = passwordEncoder;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (senha.length() < 8) {
            throw new IllegalStateException("APP_SEED_SENHA deve ter pelo menos 8 caracteres");
        }
        if (usuarios.existsByLogin("locadora")) {
            return;
        }
        String hash = passwordEncoder.encode(senha);
        Empresa empresa = usuarios.save(new Empresa("locadora", hash, "Locadora Horizonte", "11222333000181"));
        Banco banco = usuarios.save(new Banco("banco", hash, "Banco Aliança", "45997418000153"));
        usuarios.save(new Cliente("cliente", hash, "Marina Duarte", "MG-12.345.678", "52998224725",
                "Rua das Acácias, 120, Belo Horizonte", "Analista de sistemas",
                List.of(new Rendimento("Tecnova Sistemas", new BigDecimal("6800.00")))));

        automoveis.saveAll(List.of(
                new Automovel("MAT-0001", 2023, "Fiat", "Argo", "RIO2A18", empresa),
                new Automovel("MAT-0002", 2024, "Chevrolet", "Onix", "QWE1R34", empresa),
                new Automovel("MAT-0003", 2022, "Volkswagen", "T-Cross", "HJK4L56", empresa),
                new Automovel("MAT-0004", 2024, "Toyota", "Corolla", "BNM7P89", banco)));
    }
}
