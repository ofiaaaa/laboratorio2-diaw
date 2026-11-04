# Diagrama de Classes

Modelo de domínio, implementado no pacote `br.pucminas.aluguel.model`.

```mermaid
classDiagram
    direction TB

    class Usuario {
        <<abstract>>
        -Long id
        -String login
        -String senha
        -String nome
        +getTipo() TipoUsuario*
        +isAgente() boolean
    }
    class Cliente {
        -String rg
        -String cpf
        -String endereco
        -String profissao
        +substituirRendimentos(novos) void
    }
    class Rendimento {
        <<embeddable>>
        -String entidadeEmpregadora
        -BigDecimal valor
    }
    class Agente {
        <<abstract>>
        -String cnpj
    }
    class Empresa
    class Banco
    class Automovel {
        -Long id
        -String matricula
        -int ano
        -String marca
        -String modelo
        -String placa
        +isDisponivelParaAluguel() boolean
    }
    class PedidoAluguel {
        -Long id
        -LocalDate dataInicio
        -LocalDate dataFim
        -BigDecimal valorTotal
        -StatusPedido status
        -String parecer
        -Instant criadoEm
        +alterarPorCliente(...) void
        +alterarPorAgente(...) void
        +cancelar() void
        +avaliar(agente, aprovado, parecer) void
        +registrarContrato(contrato) void
    }
    class Contrato {
        -Long id
        -TipoUsuario tipoProprietario
        -LocalDate dataAssinatura
    }
    class ContratoCredito {
        -Long id
        -BigDecimal valor
        -BigDecimal taxaJurosMensal
        -int parcelas
    }
    class StatusPedido {
        <<enumeration>>
        EM_ANALISE
        APROVADO
        REPROVADO
        CANCELADO
        CONTRATADO
    }
    class TipoUsuario {
        <<enumeration>>
        CLIENTE
        EMPRESA
        BANCO
    }

    Usuario <|-- Cliente
    Usuario <|-- Agente
    Agente <|-- Empresa
    Agente <|-- Banco

    Cliente "1" *-- "0..3" Rendimento : aufere
    Cliente "1" --> "0..*" PedidoAluguel : introduz
    PedidoAluguel "0..*" --> "1" Automovel : solicita
    Automovel "0..*" --> "1" Usuario : proprietário
    PedidoAluguel "0..*" --> "0..1" Agente : avaliado por
    PedidoAluguel "1" -- "0..1" Contrato : gera
    Contrato "0..*" --> "1" Agente : executado por
    Contrato "0..*" --> "1" Usuario : proprietário registrado
    Contrato "1" *-- "0..1" ContratoCredito : associado a
    ContratoCredito "0..*" --> "1" Banco : concedido por
    PedidoAluguel ..> StatusPedido
    Contrato ..> TipoUsuario
```

## Ciclo de vida do pedido

```mermaid
stateDiagram-v2
    [*] --> EM_ANALISE : cliente introduz
    EM_ANALISE --> EM_ANALISE : cliente ou agente modifica
    EM_ANALISE --> APROVADO : parecer positivo
    EM_ANALISE --> REPROVADO : parecer negativo
    EM_ANALISE --> CANCELADO : cliente cancela
    APROVADO --> APROVADO : agente modifica
    APROVADO --> CANCELADO : cliente cancela
    APROVADO --> CONTRATADO : agente executa contrato
    REPROVADO --> [*]
    CANCELADO --> [*]
    CONTRATADO --> [*]
```

## Decisões de modelagem

- **`valorTotal` no pedido:** o enunciado não cita valor, mas a análise financeira precisa comparar o custo do aluguel com a renda do cliente.
- **`Rendimento` como objeto embutido:** só existe como parte de um cliente e é sempre substituído em conjunto, então não precisa de identidade própria.
- **Herança `JOINED`:** cada subclasse de `Usuario` tem sua tabela, e os dados comuns ficam em `usuarios`. Assim, `Automovel` e `Contrato` podem referenciar qualquer tipo de proprietário.
- **Regras de estado em `PedidoAluguel`:** as transições ficam na própria entidade, o que impede que um serviço deixe o pedido em um estado inválido.
- **`@Version` em `PedidoAluguel`:** se dois agentes alterarem o mesmo pedido ao mesmo tempo, o segundo recebe um aviso em vez de sobrescrever o primeiro.

## Revisões

| Versão | Alteração |
| :--- | :--- |
| 1.0 | Versão inicial, derivada do enunciado. |
| 1.1 | Inclusão de `valorTotal` e `@Version` em `PedidoAluguel`, e do diagrama de estados do pedido. |
