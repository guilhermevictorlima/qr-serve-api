# Tasks: Criacao e entrada sessao

> feature: criacao-e-entrada-sessao

## T-001 — Anotar com @spec os testes de acesso à sessão [concluida]
- Refs: AC-001, AC-002, AC-003, AC-004, AC-011
- Arquivos: src/test/java/br/com/qrserve/application/sessao/AcessarSessaoUseCaseTest.java, src/main/java/br/com/qrserve/application/sessao/AcessarSessaoCommand.java, src/main/java/br/com/qrserve/application/sessao/AcessarSessaoResult.java, src/main/java/br/com/qrserve/application/sessao/AcessarSessaoUseCase.java, src/main/java/br/com/qrserve/application/sessao/StatusAcessoSessao.java, src/main/java/br/com/qrserve/application/time/TimeProvider.java, src/main/java/br/com/qrserve/infrastructure/time/SystemTimeProvider.java, src/main/java/br/com/qrserve/infrastructure/config/TimeConfig.java, src/main/java/br/com/qrserve/domain/sessao/SessaoMesa.java, src/main/java/br/com/qrserve/domain/sessao/TokenSessao.java, src/main/java/br/com/qrserve/domain/sessao/TokenUsuario.java, src/main/java/br/com/qrserve/domain/sessao/ParticipanteSessao.java, src/main/java/br/com/qrserve/domain/mesa/Mesa.java, src/main/java/br/com/qrserve/infrastructure/persistence/mesa/MesaRepository.java, src/main/java/br/com/qrserve/infrastructure/persistence/sessao/SessaoMesaRepository.java, src/main/java/br/com/qrserve/infrastructure/persistence/sessao/ParticipanteSessaoRepository.java, src/main/java/br/com/qrserve/presentation/sessao/form/AcessarSessaoMesaForm.java, src/main/java/br/com/qrserve/presentation/sessao/response/AcessarSessaoMesaResponse.java, src/main/java/br/com/qrserve/presentation/sessao/response/AcessarSessaoMesaResponseStatus.java, src/main/java/br/com/qrserve/domain/exception/MesaNaoEncontradaException.java, src/main/java/br/com/qrserve/domain/estabelecimento/Estabelecimento.java
- Notas: código já existe (`AcessarSessaoUseCase`). cenario_1→AC-001, cenario_4→AC-002, cenario_2→AC-003, cenario_3→AC-004. AC-011 precisa de teste novo (novo acesso cria nova solicitação).

## T-002 — Anotar com @spec os testes de resposta à solicitação [concluida]
- Refs: AC-007, AC-008, AC-009
- Arquivos: src/test/java/br/com/qrserve/application/sessao/ResponderSolicitacaoEntradaSessaoUseCaseTest.java, src/main/java/br/com/qrserve/application/sessao/ResponderSolicitacaoEntradaSessaoCommand.java, src/main/java/br/com/qrserve/application/sessao/RespostaSolicitacao.java, src/main/java/br/com/qrserve/application/sessao/FeedbackSender.java, src/main/java/br/com/qrserve/presentation/sessao/form/ResponderSolicitacaoEntradaSessaoForm.java, src/main/java/br/com/qrserve/presentation/sessao/form/RespostaSolicitacaoEntradaSessao.java, src/main/java/br/com/qrserve/domain/exception/ParticipanteSessaoNaoEncontradoException.java, src/main/java/br/com/qrserve/domain/exception/SolicitacaoEntradaSessaoNaoEncontradaException.java, src/main/java/br/com/qrserve/domain/exception/BusinessException.java, src/main/java/br/com/qrserve/domain/exception/RecursoNaoEncontradoException.java, src/main/java/br/com/qrserve/presentation/exception/GlobalExceptionHandler.java, src/main/java/br/com/qrserve/presentation/exception/ErroResponse.java
- Notas: cenario_1→AC-007, cenario_2→AC-008, cenario_4 e cenario_5→AC-009.

## T-003 — Listar solicitações pendentes da sessão [concluida]
- Refs: US-003, AC-005
- Arquivos: src/main/java/br/com/qrserve/application/sessao/ListarSolicitacoesPendentesUseCase.java, src/main/java/br/com/qrserve/application/sessao/ListarSolicitacoesPendentesCommand.java, src/main/java/br/com/qrserve/infrastructure/persistence/sessao/SolicitacaoEntradaSessaoRepository.java, src/main/java/br/com/qrserve/presentation/sessao/SessaoMesaController.java, src/main/java/br/com/qrserve/presentation/sessao/response/SolicitacaoEntradaSessaoResponse.java, src/test/java/br/com/qrserve/application/sessao/ListarSolicitacoesPendentesUseCaseTest.java, src/test/java/br/com/qrserve/presentation/sessao/SessaoMesaControllerTest.java
- Notas: não há endpoint de consulta hoje. TDD (P-003): teste primeiro.

## T-004 — Expirar solicitação pendente em 3 minutos [concluida]
- Refs: AC-010
- Arquivos: src/main/java/br/com/qrserve/domain/sessao/SolicitacaoEntradaSessao.java, src/main/java/br/com/qrserve/application/sessao/ResponderSolicitacaoEntradaSessaoUseCase.java, src/test/java/br/com/qrserve/application/sessao/ResponderSolicitacaoEntradaSessaoUseCaseTest.java, src/main/java/br/com/qrserve/domain/exception/EstadoInvalidoException.java
- Notas: não existe expiração hoje. Conflita em arquivo de teste com T-002 (rodar depois). T-003 deve filtrar expiradas: rodar depois de T-004.

## T-005 — Anotar com @spec os testes de listagem de participantes [concluida]
- Refs: US-005, AC-012, AC-013
- Arquivos: src/test/java/br/com/qrserve/presentation/participantesessao/ParticipanteSessaoControllerTest.java, src/main/java/br/com/qrserve/presentation/participantesessao/ParticipanteSessaoController.java, src/main/java/br/com/qrserve/presentation/participantesessao/response/ParticipanteSessaoResponse.java
- Notas: código e testes já existem. O endpoint GET /participantes/{sessaoId} não valida token do consultante (ver pergunta Q-003).
