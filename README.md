# vant.bank

Internet banking full stack: API REST em Java 21 + Spring Boot e interface web em HTML, CSS e JavaScript puro. O sistema controla contas correntes e poupanças, depósitos, saques, transferências entre contas, extrato com filtros e um painel com os principais números do banco.

**Demonstração:** https://idalinosou.github.io/vant.bank/

A demonstração no GitHub Pages funciona sem servidor: os dados de exemplo ficam salvos no `localStorage` do navegador. A API real roda em Spring Boot e pode ser executada localmente.

## Funcionalidades

- Painel com saldo total, contas ativas, bloqueadas, volume movimentado e transferências do mês
- Contas com busca por número, titular ou documento, filtro por status e paginação
- Cadastro e edição de contas correntes e poupanças, com limite de cheque especial
- Status da conta: ativa, bloqueada e encerrada, com transições controladas
- Depósitos e saques com uso automático do limite disponível
- Transferências entre contas com débito e crédito atômicos e recibo com os saldos atualizados
- Extrato por conta com filtros de depósitos, saques e transferências enviadas ou recebidas
- Saldo após cada movimentação registrado no extrato
- Encerramento de conta somente com saldo zerado
- Validações, mensagens de erro claras e confirmação em duas etapas para bloqueio e encerramento
- Interface responsiva, acessível e com tema escuro

## Stack

- Java 21
- Spring Boot 3.4
- Spring Web, Spring Data JPA e Bean Validation
- H2 Database
- Maven
- HTML, CSS e JavaScript sem frameworks

## Como rodar

Pré-requisitos: Java 21 e Maven.

```bash
mvn spring-boot:run
```

Depois acesse http://localhost:8084.

O banco fica em `./dados/vantbank.mv.db` e já começa com contas e movimentações de exemplo. Para recomeçar do zero, pare a aplicação e apague a pasta `dados`. O console do H2 fica em http://localhost:8084/h2-console.

## Testes

```bash
mvn test
```

São 25 testes de integração com MockMvc cobrindo cadastro de contas, duplicidade, validações, filtros, paginação, transições de status, regra de encerramento, depósitos, saques com limite, transferências, extrato e agregações do painel.

## API

| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/api/painel` | Indicadores do banco |
| GET | `/api/contas` | Lista contas com `busca`, `status`, `pagina` e `tamanho` |
| GET | `/api/contas/{id}` | Busca uma conta |
| POST | `/api/contas` | Cadastra uma conta |
| PUT | `/api/contas/{id}` | Atualiza os dados da conta |
| PATCH | `/api/contas/{id}/status` | Altera o status da conta |
| POST | `/api/contas/{id}/depositos` | Registra um depósito |
| POST | `/api/contas/{id}/saques` | Registra um saque |
| GET | `/api/contas/{id}/extrato` | Extrato com `tipo`, `pagina` e `tamanho` |
| POST | `/api/transferencias` | Transfere entre duas contas |
| GET | `/api/transferencias/recentes` | Últimas transferências enviadas |

## Regras de negócio

- O número da conta é único.
- Conta poupança não possui limite de cheque especial.
- Conta encerrada não pode ser editada e não recebe novas movimentações.
- Ativa pode ser bloqueada ou encerrada; bloqueada pode ser reativada ou encerrada; encerrada é estado final.
- O encerramento exige saldo zerado.
- Depósitos e saques só são aceitos em contas ativas.
- O saque pode usar o limite: o saldo pode ficar negativo até o valor do limite.
- A transferência bloqueia as duas contas na mesma transação e grava uma movimentação de saída na origem e outra de entrada no destino.
- A conta de origem e a de destino precisam ser diferentes e ambas precisam estar ativas.
- O volume do mês soma depósitos, saques e transferências recebidas; as transferências do mês contam apenas as enviadas.
- O saldo total do painel ignora contas encerradas.

## Estrutura

```
src/main/java/dev/vant/bank
├── conta          contas, status, validações e endpoints
├── movimentacao   movimentações e consulta de extrato
├── operacao       depósitos, saques e regras de saldo
├── transferencia  transferências entre contas
├── painel         indicadores do banco
├── dados          carga inicial de exemplo
└── erro           tratamento centralizado de erros
```

A interface fica em `src/main/resources/static` e é copiada para `docs/` na publicação do GitHub Pages.
