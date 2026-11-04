# Diagrama de Casos de Uso

O diagrama usa Mermaid, que o GitHub renderiza direto no arquivo. Atores ficam nas pontas e os casos de uso, em formato oval. **Empresa** e **Banco** são especializações de **Agente**.

```mermaid
flowchart LR
    visitante(["Visitante"])
    cliente(["Cliente"])
    agente(["Agente"])
    empresa(["Empresa"])
    banco(["Banco"])

    empresa -. especializa .-> agente
    banco -. especializa .-> agente

    subgraph sistema["Sistema de Aluguel de Carros"]
        uc01(["UC01 Cadastrar-se como cliente"])
        uc02(["UC02 Fazer login"])
        uc03(["UC03 Manter dados pessoais"])
        uc04(["UC04 Introduzir pedido"])
        uc05(["UC05 Consultar pedidos e status"])
        uc06(["UC06 Modificar pedido"])
        uc07(["UC07 Cancelar pedido"])
        uc08(["UC08 Avaliar pedido"])
        uc09(["UC09 Executar contrato"])
        uc10(["UC10 Conceder contrato de crédito"])
        uc11(["UC11 Cadastrar automóvel"])
        uc12(["UC12 Consultar clientes"])
    end

    visitante --> uc01
    cliente --> uc02
    cliente --> uc03
    cliente --> uc04
    cliente --> uc05
    cliente --> uc06
    cliente --> uc07

    agente --> uc02
    agente --> uc05
    agente --> uc06
    agente --> uc08
    agente --> uc09
    agente --> uc11
    agente --> uc12

    banco --> uc10
    uc10 -. estende .-> uc09
```

## Revisões

| Versão | Alteração |
| :--- | :--- |
| 1.0 | Versão inicial, derivada do enunciado. |
| 1.1 | Inclusão de UC11 e UC12: os pedidos dependem de uma frota cadastrada, e a análise financeira depende dos dados do cliente. |
