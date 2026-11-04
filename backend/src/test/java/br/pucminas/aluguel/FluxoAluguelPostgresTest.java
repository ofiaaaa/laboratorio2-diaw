package br.pucminas.aluguel;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Repete o fluxo em um PostgreSQL real, com a mesma geração de schema usada no Supabase,
 * para detectar diferenças de SQL que o H2 não acusa.
 */
class FluxoAluguelPostgresTest extends FluxoAluguelTest {

    private static final EmbeddedPostgres POSTGRES = iniciarPostgres();

    private static EmbeddedPostgres iniciarPostgres() {
        try {
            return EmbeddedPostgres.builder().start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void usarPostgres(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registro.add("spring.datasource.username", () -> "postgres");
        registro.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registro.add("spring.jpa.properties.hibernate.hbm2ddl.create_namespaces", () -> "true");
    }
}
