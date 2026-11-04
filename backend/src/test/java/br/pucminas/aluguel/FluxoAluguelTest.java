package br.pucminas.aluguel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.pucminas.aluguel.model.Automovel;
import br.pucminas.aluguel.model.Banco;
import br.pucminas.aluguel.model.Empresa;
import br.pucminas.aluguel.repository.AutomovelRepository;
import br.pucminas.aluguel.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoAluguelTest {

    private static final String SENHA = "senha-de-teste";
    private static final AtomicInteger SEQUENCIA = new AtomicInteger();
    private static final AtomicInteger SEQUENCIA_CPF = new AtomicInteger();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private AutomovelRepository automoveis;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenEmpresa;
    private String tokenBanco;
    private Long empresaId;
    private Long automovelId;
    private int sufixo;

    @BeforeEach
    void prepararAgentes() throws Exception {
        sufixo = SEQUENCIA.incrementAndGet();
        String hash = passwordEncoder.encode(SENHA);
        Empresa empresa = usuarios.save(new Empresa("empresa" + sufixo, hash, "Empresa " + sufixo, cnpj(1)));
        usuarios.save(new Banco("banco" + sufixo, hash, "Banco " + sufixo, cnpj(2)));
        empresaId = empresa.getId();
        Automovel automovel = automoveis.save(new Automovel("MAT-" + sufixo, 2024, "Fiat", "Argo",
                String.format("ABC1D%02d", sufixo), empresa));
        automovelId = automovel.getId();
        tokenEmpresa = login("empresa" + sufixo);
        tokenBanco = login("banco" + sufixo);
    }

    @Test
    void acessoSemLoginENegado() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/automoveis")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clientes/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginComSenhaErradaEhRecusado() throws Exception {
        criarCliente("login-errado" + sufixo);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"login-errado%d","senha":"senha-incorreta"}""".formatted(sufixo)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Usuário ou senha inválidos"));
    }

    @Test
    void crudDeCliente() throws Exception {
        String login = "crud" + sufixo;
        Long id = criarCliente(login);
        String token = login(login);

        chamar(get("/api/clientes/" + id), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cliente de Teste"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.rendimentos.length()").value(1));

        chamar(put("/api/clientes/" + id), token, """
                {"nome":"Nome Novo","rg":"RG-%d","cpf":"%s","endereco":"Rua B, 2","profissao":"Designer",
                 "rendimentos":[{"entidadeEmpregadora":"A","valor":1000},{"entidadeEmpregadora":"B","valor":2000}]}
                """.formatted(sufixo, cpfLivre()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"))
                .andExpect(jsonPath("$.rendimentos.length()").value(2));

        chamar(get("/api/clientes"), tokenEmpresa).andExpect(status().isOk());
        chamar(get("/api/clientes"), token).andExpect(status().isForbidden());

        chamar(delete("/api/clientes/" + id), token).andExpect(status().isNoContent());
        // O token ainda é válido, mas a conta não existe mais
        chamar(get("/api/auth/me"), token).andExpect(status().isUnauthorized());
    }

    @Test
    void clienteAceitaNoMaximoTresRendimentos() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"login":"muitos%1$d","senha":"%2$s","nome":"Muitos","rg":"X%1$d","cpf":"%3$s",
                         "endereco":"Rua","profissao":"Dev","rendimentos":[
                         {"entidadeEmpregadora":"A","valor":1},{"entidadeEmpregadora":"B","valor":1},
                         {"entidadeEmpregadora":"C","valor":1},{"entidadeEmpregadora":"D","valor":1}]}
                        """.formatted(sufixo, SENHA, cpfLivre())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.rendimentos").exists());
    }

    @Test
    void cpfInvalidoOuDuplicadoEhRecusado() throws Exception {
        String corpo = """
                {"login":"%s","senha":"%s","nome":"Teste","rg":"%s","cpf":"%s",
                 "endereco":"Rua","profissao":"Dev","rendimentos":[]}""";
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo.formatted("cpfruim" + sufixo, SENHA, "R" + sufixo, "11111111111")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").value("CPF inválido"));

        String cpf = cpfLivre();
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo.formatted("cpf1" + sufixo, SENHA, "R1" + sufixo, cpf)))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo.formatted("cpf2" + sufixo, SENHA, "R2" + sufixo, cpf)))
                .andExpect(status().isConflict());
    }

    @Test
    void clienteNaoEnxergaNemAlteraDadosDeOutroCliente() throws Exception {
        Long donoId = criarCliente("dono" + sufixo);
        criarCliente("outro" + sufixo);
        String tokenDono = login("dono" + sufixo);
        String tokenOutro = login("outro" + sufixo);
        Long pedidoId = criarPedido(tokenDono, 10, 15);

        chamar(get("/api/clientes/" + donoId), tokenOutro).andExpect(status().isForbidden());
        chamar(delete("/api/clientes/" + donoId), tokenOutro).andExpect(status().isForbidden());
        chamar(get("/api/pedidos/" + pedidoId), tokenOutro).andExpect(status().isNotFound());
        chamar(post("/api/pedidos/" + pedidoId + "/cancelar"), tokenOutro).andExpect(status().isNotFound());
        chamar(put("/api/pedidos/" + pedidoId), tokenOutro, corpoPedido(10, 15, 500)).andExpect(status().isNotFound());
        chamar(get("/api/pedidos"), tokenOutro).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void clienteModificaECancelaPedido() throws Exception {
        criarCliente("altera" + sufixo);
        String token = login("altera" + sufixo);
        Long pedidoId = criarPedido(token, 10, 15);

        chamar(get("/api/pedidos/" + pedidoId), token)
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));

        chamar(put("/api/pedidos/" + pedidoId), token, corpoPedido(20, 25, 900))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(900));

        chamar(post("/api/pedidos/" + pedidoId + "/cancelar"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        chamar(put("/api/pedidos/" + pedidoId), token, corpoPedido(20, 25, 900))
                .andExpect(status().isConflict());
    }

    @Test
    void periodoConflitanteEhRecusadoEPeriodoLiberaAposCancelamento() throws Exception {
        criarCliente("periodo" + sufixo);
        String token = login("periodo" + sufixo);
        Long pedidoId = criarPedido(token, 10, 15);

        chamar(post("/api/pedidos"), token, corpoPedido(14, 20, 500)).andExpect(status().isConflict());
        chamar(post("/api/pedidos"), token, corpoPedido(16, 20, 500)).andExpect(status().isCreated());
        chamar(put("/api/pedidos/" + pedidoId), token, corpoPedido(11, 14, 500)).andExpect(status().isOk());

        chamar(post("/api/pedidos/" + pedidoId + "/cancelar"), token).andExpect(status().isOk());
        chamar(post("/api/pedidos"), token, corpoPedido(10, 14, 500)).andExpect(status().isCreated());
    }

    @Test
    void datasInvalidasSaoRecusadas() throws Exception {
        criarCliente("datas" + sufixo);
        String token = login("datas" + sufixo);
        chamar(post("/api/pedidos"), token, corpoPedido(15, 10, 500)).andExpect(status().isBadRequest());
        chamar(post("/api/pedidos"), token, corpoPedido(-5, 10, 500)).andExpect(status().isBadRequest());
    }

    @Test
    void permissoesPorPapel() throws Exception {
        criarCliente("papel" + sufixo);
        String token = login("papel" + sufixo);
        Long pedidoId = criarPedido(token, 10, 15);

        chamar(post("/api/pedidos"), tokenEmpresa, corpoPedido(30, 35, 500)).andExpect(status().isForbidden());
        chamar(post("/api/pedidos/" + pedidoId + "/cancelar"), tokenEmpresa).andExpect(status().isForbidden());
        chamar(post("/api/pedidos/" + pedidoId + "/avaliar"), token, """
                {"aprovado":true,"parecer":"Autoaprovado"}""").andExpect(status().isForbidden());
        chamar(post("/api/automoveis"), token, """
                {"matricula":"M","ano":2024,"marca":"A","modelo":"B","placa":"ABC1D23"}""")
                .andExpect(status().isForbidden());
    }

    @Test
    void agenteAvaliaEEmpresaExecutaContrato() throws Exception {
        criarCliente("contrato" + sufixo);
        String token = login("contrato" + sufixo);
        Long pedidoId = criarPedido(token, 10, 15);

        // Sem parecer positivo não há contrato
        chamar(post("/api/pedidos/" + pedidoId + "/contrato"), tokenEmpresa, """
                {"tipoProprietario":"EMPRESA"}""").andExpect(status().isConflict());

        chamar(put("/api/pedidos/" + pedidoId), tokenEmpresa, corpoPedido(10, 16, 1200))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(1200));

        chamar(post("/api/pedidos/" + pedidoId + "/avaliar"), tokenEmpresa, """
                {"aprovado":true,"parecer":"Renda compativel"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.parecer").value("Renda compativel"));

        // Empresa não concede crédito e não registra em nome de banco
        chamar(post("/api/pedidos/" + pedidoId + "/contrato"), tokenEmpresa, """
                {"tipoProprietario":"EMPRESA","credito":{"valor":1000,"taxaJurosMensal":2,"parcelas":12}}""")
                .andExpect(status().isForbidden());
        chamar(post("/api/pedidos/" + pedidoId + "/contrato"), tokenEmpresa, """
                {"tipoProprietario":"BANCO"}""").andExpect(status().isBadRequest());

        chamar(post("/api/pedidos/" + pedidoId + "/contrato"), tokenEmpresa, """
                {"tipoProprietario":"EMPRESA"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONTRATADO"))
                .andExpect(jsonPath("$.contrato.tipoProprietario").value("EMPRESA"))
                .andExpect(jsonPath("$.contrato.credito").doesNotExist());

        chamar(post("/api/pedidos/" + pedidoId + "/cancelar"), token).andExpect(status().isConflict());

        // O cliente acompanha o status e os dados do contrato
        chamar(get("/api/pedidos/" + pedidoId), token)
                .andExpect(jsonPath("$.status").value("CONTRATADO"))
                .andExpect(jsonPath("$.contrato.agenteNome").value("Empresa " + sufixo));
    }

    @Test
    void bancoConcedeCreditoEFicaComAPropriedade() throws Exception {
        criarCliente("credito" + sufixo);
        String token = login("credito" + sufixo);
        Long pedidoId = criarPedido(token, 10, 15);

        chamar(post("/api/pedidos/" + pedidoId + "/avaliar"), tokenBanco, """
                {"aprovado":true,"parecer":"Credito aprovado"}""").andExpect(status().isOk());

        chamar(post("/api/pedidos/" + pedidoId + "/contrato"), tokenBanco, """
                {"tipoProprietario":"BANCO","credito":{"valor":30000,"taxaJurosMensal":1.5,"parcelas":48}}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contrato.tipoProprietario").value("BANCO"))
                .andExpect(jsonPath("$.contrato.credito.bancoNome").value("Banco " + sufixo))
                .andExpect(jsonPath("$.contrato.credito.parcelas").value(48));

        assertThat(proprietarioDoAutomovel()).isEqualTo("Banco " + sufixo);
        // O automóvel continua disponível para outros períodos
        chamar(post("/api/pedidos"), token, corpoPedido(20, 25, 500)).andExpect(status().isCreated());
    }

    @Test
    void automovelDoClienteSaiDaListaDeAluguel() throws Exception {
        criarCliente("comprador" + sufixo);
        String token = login("comprador" + sufixo);
        Long pedidoId = criarPedido(token, 10, 15);
        chamar(post("/api/pedidos/" + pedidoId + "/avaliar"), tokenEmpresa, """
                {"aprovado":true,"parecer":"Ok"}""").andExpect(status().isOk());

        // Outro pedido ativo do mesmo automóvel impede a transferência
        Long outroPedido = criarPedido(token, 20, 25);
        chamar(post("/api/pedidos/" + pedidoId + "/contrato"), tokenEmpresa, """
                {"tipoProprietario":"CLIENTE"}""").andExpect(status().isConflict());
        chamar(post("/api/pedidos/" + outroPedido + "/cancelar"), token).andExpect(status().isOk());

        chamar(post("/api/pedidos/" + pedidoId + "/contrato"), tokenEmpresa, """
                {"tipoProprietario":"CLIENTE"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contrato.tipoProprietario").value("CLIENTE"));

        String lista = chamar(get("/api/automoveis"), token).andReturn().getResponse().getContentAsString();
        assertThat(JsonPath.<java.util.List<Integer>>read(lista, "$[*].id")).doesNotContain(automovelId.intValue());
        chamar(post("/api/pedidos"), token, corpoPedido(30, 35, 500)).andExpect(status().isConflict());
    }

    @Test
    void pedidoReprovadoNaoViraContratoEClienteComPedidoNaoEhExcluido() throws Exception {
        Long id = criarCliente("reprovado" + sufixo);
        String token = login("reprovado" + sufixo);
        Long pedidoId = criarPedido(token, 10, 15);

        chamar(post("/api/pedidos/" + pedidoId + "/avaliar"), tokenBanco, """
                {"aprovado":false,"parecer":"Renda insuficiente"}""")
                .andExpect(jsonPath("$.status").value("REPROVADO"))
                .andExpect(jsonPath("$.agenteAvaliadorNome").value("Banco " + sufixo));

        chamar(post("/api/pedidos/" + pedidoId + "/avaliar"), tokenBanco, """
                {"aprovado":true,"parecer":"Mudei de ideia"}""").andExpect(status().isConflict());
        chamar(post("/api/pedidos/" + pedidoId + "/contrato"), tokenBanco, """
                {"tipoProprietario":"BANCO"}""").andExpect(status().isConflict());
        chamar(delete("/api/clientes/" + id), token).andExpect(status().isConflict());
    }

    @Test
    void agenteCadastraAutomovelComPlacaNormalizada() throws Exception {
        String placa = String.format("XYZ-1A%02d", sufixo);
        chamar(post("/api/automoveis"), tokenEmpresa, """
                {"matricula":"NOVA-%d","ano":2025,"marca":"Honda","modelo":"Civic","placa":"%s"}
                """.formatted(sufixo, placa.toLowerCase()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value(placa.replace("-", "")))
                .andExpect(jsonPath("$.proprietarioTipo").value("EMPRESA"));
        chamar(post("/api/automoveis"), tokenEmpresa, """
                {"matricula":"OUTRA-%d","ano":2025,"marca":"Honda","modelo":"Civic","placa":"%s"}
                """.formatted(sufixo, placa)).andExpect(status().isConflict());
    }

    private String proprietarioDoAutomovel() throws Exception {
        String lista = chamar(get("/api/automoveis"), tokenEmpresa).andReturn().getResponse().getContentAsString();
        return JsonPath.<java.util.List<String>>read(lista,
                "$[?(@.id == " + automovelId + ")].proprietarioNome").getFirst();
    }

    private Long criarCliente(String login) throws Exception {
        String resposta = mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"login":"%s","senha":"%s","nome":"Cliente de Teste","rg":"RG-%s","cpf":"%s",
                         "endereco":"Rua A, 1","profissao":"Dev",
                         "rendimentos":[{"entidadeEmpregadora":"Empresa X","valor":5000}]}
                        """.formatted(login, SENHA, login, cpfLivre())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private Long criarPedido(String token, int diasInicio, int diasFim) throws Exception {
        String resposta = chamar(post("/api/pedidos"), token, corpoPedido(diasInicio, diasFim, 500))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private String corpoPedido(int diasInicio, int diasFim, int valor) {
        return """
                {"automovelId":%d,"dataInicio":"%s","dataFim":"%s","valorTotal":%d}
                """.formatted(automovelId, LocalDate.now().plusDays(diasInicio), LocalDate.now().plusDays(diasFim), valor);
    }

    private String login(String login) throws Exception {
        String resposta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"%s","senha":"%s"}""".formatted(login, SENHA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private ResultActions chamar(MockHttpServletRequestBuilder requisicao, String token) throws Exception {
        return mvc.perform(requisicao.header("Authorization", "Bearer " + token));
    }

    private ResultActions chamar(MockHttpServletRequestBuilder requisicao, String token, String corpo) throws Exception {
        return chamar(requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo), token);
    }

    private String cnpj(int tipo) {
        return String.format("%012d%02d", sufixo, tipo);
    }

    /** Gera um CPF válido e diferente a cada chamada, calculando os dígitos verificadores. */
    private String cpfLivre() {
        String base = String.format("%09d", 100_000_000 + SEQUENCIA_CPF.incrementAndGet() * 1_013);
        int primeiro = digitoVerificador(base, 10);
        return base + primeiro + digitoVerificador(base + primeiro, 11);
    }

    private int digitoVerificador(String numeros, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < numeros.length(); i++) {
            soma += Character.getNumericValue(numeros.charAt(i)) * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
