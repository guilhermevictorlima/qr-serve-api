# Spec: Criacao e entrada sessao

> feature: criacao-e-entrada-sessao
> status: rascunho

## Contexto

Clientes de bares e restaurantes escaneiam o QR fixo da mesa para abrir ou entrar na sessão da mesa. A sessão fica aberta nos primeiros 3 minutos; depois, novos usuários precisam de aprovação dos participantes atuais.

## Histórias

### US-001 — Criar a sessão ao escanear o QR da mesa

Como cliente, quero escanear o QR da mesa e abrir a sessão, para que eu possa pedir pelo celular.

#### AC-001 — Primeiro acesso cria a sessão

- **Dado** uma mesa com token válido e sem sessão ativa
- **Quando** o primeiro cliente escaneia o QR
- **Então** uma sessão é criada, aberta, e o cliente entra como participante (sessão com instante de abertura registrado)

#### AC-002 — Mesa inexistente é recusada

- **Dado** um QR que não corresponde a nenhuma mesa
- **Quando** alguém acessa com esse QR
- **Então** o acesso é recusado e nenhuma sessão é criada (not found)

### US-002 — Entrar livremente na janela de 3 minutos

Como cliente, quero entrar direto na sessão da minha mesa logo após ela ser aberta, para que o grupo peça junto sem burocracia.

#### AC-003 — Entrada direta dentro de 3 minutos

- **Dado** uma sessão da mesa aberta há até 3 minutos
- **Quando** um novo cliente escaneia o QR
- **Então** ele entra imediatamente como participante, sem aprovação

#### AC-004 — Sessão fecha após 3 minutos

- **Dado** uma sessão da mesa aberta há mais de 3 minutos
- **Quando** um novo cliente escaneia o QR
- **Então** ele não entra direto: a tela informa que a sessão está fechada e uma solicitação de entrada é criada como pendente

### US-003 — Solicitar entrada em sessão fechada

Como cliente que chegou depois, quero pedir para entrar na sessão da mesa, para que os participantes decidam se me aceitam.

#### AC-005 — Solicitação pendente fica visível aos participantes

- **Dado** uma solicitação de entrada pendente numa sessão fechada
- **Quando** um participante atual consulta as solicitações
- **Então** ele vê a solicitação pendente do novo cliente

#### AC-010 — Solicitação pendente expira em 3 minutos

- **Dado** uma solicitação pendente criada há mais de 3 minutos
- **Quando** um participante tenta aprová-la ou recusá-la
- **Então** a resposta é rejeitada informando que a solicitação expirou, e o solicitante não entra (conflito 409)

#### AC-011 — Recusado reinicia o fluxo para pedir de novo

- **Dado** um solicitante cuja solicitação foi recusada ou expirou
- **Quando** ele escaneia o QR novamente
- **Então** uma nova solicitação pendente é criada, sem reaproveitar a anterior

### US-004 — Aprovar ou recusar solicitações

Como participante da sessão, quero aprovar ou recusar quem pede para entrar, para que só pessoas da minha mesa participem.

#### AC-007 — Aprovação admite o solicitante

- **Dado** uma solicitação pendente
- **Quando** um participante atual a aprova
- **Então** o solicitante passa a ser participante da sessão

#### AC-008 — Recusa mantém o solicitante fora

- **Dado** uma solicitação pendente
- **Quando** um participante atual a recusa
- **Então** o solicitante é notificado, não entra e a solicitação deixa de existir

#### AC-009 — Quem não participa não decide

- **Dado** uma solicitação pendente
- **Quando** alguém que não é participante tenta aprová-la
- **Então** a ação é negada e a solicitação continua pendente (forbidden)

### US-005 — Ver os participantes da sessão

Como participante da sessão, quero ver quem está na sessão da mesa, para que eu saiba com quem estou pedindo.

#### AC-012 — Participantes da sessão são listados

- **Dado** uma sessão com participantes
- **Quando** um participante consulta os participantes da sessão
- **Então** ele vê o nome de cada participante da sessão (lista com sessão, token e nome)

#### AC-013 — Quem não participa não vê os participantes

- **Dado** uma sessão com participantes
- **Quando** alguém que não é participante dessa sessão (token inexistente ou de outra sessão) consulta os participantes
- **Então** a consulta é recusada e nenhum participante é exibido (não encontrado, 404)

## Fora de escopo

- Pedidos, cardápio e painel da equipe.
- Notificação em tempo real das solicitações (nesta feature, consulta simples).
- Encerramento da sessão e geração do QR/token das mesas.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-001 | A janela de 3 minutos conta a partir da abertura da sessão | aberta | — |
| ASM-002 | Basta um participante aprovar (não unanimidade) | confirmada | Confirmado pelo dono do produto: basta um participante aprovar |
| ASM-003 | Cada acesso gera um novo token de usuário anônimo (sem login); por isso escanear de novo cria nova solicitação, sem deduplicar | aberta | Comportamento atual do código (`AcessarSessaoUseCase`); consistente com Q-002 |
| ASM-004 | Mesa sem sessão ativa sempre cria nova sessão | aberta | — |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-001 | Solicitação pendente expira? Após quanto tempo? | respondida | Expira em 3 minutos (AC-010) |
| Q-003 | A listagem de participantes (`GET /participantes/{sessaoId}`) deve exigir o token de um participante da sessão, como a listagem de solicitações exige? Hoje qualquer pessoa com o id da sessão consegue listar | respondida | Sim: exige o token de um participante da sessão (AC-012, AC-013) |
| Q-002 | Solicitante recusado pode pedir de novo? | respondida | Pode, mas precisa reiniciar o fluxo (escanear o QR / chamar a API de novo) (AC-011) |
