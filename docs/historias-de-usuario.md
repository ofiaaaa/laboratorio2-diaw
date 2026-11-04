# Histórias de Usuário

Atores: **Cliente** (usuário individual) e **Agente** (Empresa ou Banco).

## Acesso

### US01 - Cadastro de cliente
**Como** visitante, **quero** criar uma conta com meus dados, **para** poder usar o sistema.

- Exige usuário, senha, nome, RG, CPF, endereço e profissão.
- Aceita de 0 a 3 rendimentos, cada um com entidade empregadora e valor.
- Recusa CPF inválido e usuário, CPF ou RG já cadastrados.

### US02 - Login
**Como** usuário cadastrado, **quero** entrar com usuário e senha, **para** acessar as funções do meu perfil.

- Nenhuma funcionalidade fica disponível sem login, exceto o cadastro de cliente.
- Credenciais incorretas geram sempre a mesma mensagem, sem indicar qual dado está errado.
- Clientes e agentes veem menus e ações diferentes.

## Cliente

### US03 - Manter meus dados
**Como** cliente, **quero** consultar, alterar e excluir meus dados, **para** mantê-los corretos.

- Um cliente não acessa nem altera os dados de outro.
- Uma conta com pedidos registrados não pode ser excluída.

### US04 - Introduzir pedido
**Como** cliente, **quero** pedir o aluguel de um automóvel, **para** um período e valor definidos.

- O pedido começa com o status **Em análise**.
- O fim do período deve ser posterior ao início, e o início não pode estar no passado.
- O automóvel não pode ter outro pedido ativo no mesmo período.

### US05 - Consultar pedidos
**Como** cliente, **quero** ver meus pedidos e seus status, **para** acompanhar o andamento.

- O cliente vê apenas os próprios pedidos, com parecer e contrato, quando existirem.

### US06 - Modificar pedido
**Como** cliente, **quero** alterar um pedido, **para** corrigir o automóvel, as datas ou o valor.

- Permitido somente enquanto o pedido estiver **Em análise**.

### US07 - Cancelar pedido
**Como** cliente, **quero** cancelar um pedido, **para** desistir do aluguel.

- Permitido enquanto o pedido estiver **Em análise** ou **Aprovado**.
- O cancelamento libera o período do automóvel.

## Agente

### US08 - Avaliar pedido
**Como** agente, **quero** analisar financeiramente um pedido, **para** registrar um parecer positivo ou negativo.

- O agente vê a profissão, os empregadores e a renda do cliente.
- Permitido somente para pedidos **Em análise**, com parecer obrigatório.

### US09 - Modificar pedido
**Como** agente, **quero** ajustar um pedido, **para** adequá-lo à análise.

- Permitido para pedidos **Em análise** ou **Aprovados**.

### US10 - Executar contrato
**Como** agente, **quero** transformar um pedido aprovado em contrato, **para** formalizar o aluguel.

- Só pedidos com parecer positivo podem virar contrato.
- O automóvel é registrado em nome do cliente ou do agente que executa o contrato (empresa ou banco).
- Ao passar para um cliente, o automóvel sai da lista de aluguel, e por isso não pode ter outros pedidos ativos.

### US11 - Conceder crédito
**Como** banco, **quero** associar um contrato de crédito ao aluguel, **para** financiá-lo.

- Apenas bancos concedem crédito, sempre em nome do banco logado.
- O crédito registra valor, juros mensais e parcelas.

### US12 - Manter frota e consultar clientes
**Como** agente, **quero** cadastrar automóveis e consultar clientes, **para** apoiar os pedidos e a análise.

- O automóvel registra matrícula, ano, marca, modelo, placa e proprietário.
- Placa e matrícula não se repetem.
