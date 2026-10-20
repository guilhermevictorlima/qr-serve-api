# QR-Serve
Cardápio digital interativo por QR Code, um por mesa, para bares e restaurantes. O cliente pede direto pelo celular (navegador, sem app e sem tablet). A equipe acompanha tudo em um painel em tempo real.
## Ideia central
Cada mesa tem um QR fixo com token não adivinhável. O scan cria ou conecta a uma **sessão da mesa**. A sessão é criada no primeiro acesso, fica aberta nos primeiros 3 minutos e depois é fechada, requerindo aprovação dos participantes atuais da sessão caso um novo usuário tente entrar.
## Convenções de Projeto
### Conventional Commits
Todos os commits devem ser atômicos e escritos em tom imperativo, seguindo o padrão conventional commits, utilizando o identificador da task.
#### Exemplo
- Branch: **debt/QT-1-aplicacao-sdd**
- commit: `chore(QT-1): criar gate no CLI`
