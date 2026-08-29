--------------------------------------------------------------------
-------------- CARDAPIO --------------
--------------------------------------------------------------------
CREATE TABLE categoria
(
    id        INTEGER GENERATED ALWAYS AS IDENTITY,
    descricao VARCHAR(255) NOT NULL,

    CONSTRAINT pk_categoria PRIMARY KEY (id)
);

CREATE TABLE item_cardapio
(
    id           INTEGER GENERATED ALWAYS AS IDENTITY,
    categoria_id INTEGER        NOT NULL,
    titulo       VARCHAR(255)   NOT NULL,
    descricao    TEXT,
    preco        DECIMAL(10, 2) NOT NULL,

    CONSTRAINT pk_item_cardapio PRIMARY KEY (id),
    CONSTRAINT fk_item_cardapio_categoria FOREIGN KEY (categoria_id)
        REFERENCES categoria (id) ON DELETE RESTRICT
);

CREATE INDEX idx_item_cardapio_categoria_id ON item_cardapio (categoria_id);

CREATE TABLE item_cardapio_imagem
(
    id               INTEGER GENERATED ALWAYS AS IDENTITY,
    item_cardapio_id INTEGER      NOT NULL,
    url              VARCHAR(500) NOT NULL,
    ordem_exibicao   INTEGER      NOT NULL DEFAULT 0,

    CONSTRAINT pk_item_cardapio_imagem PRIMARY KEY (id),
    CONSTRAINT fk_item_cardapio_imagem_item_cardapio FOREIGN KEY (item_cardapio_id)
        REFERENCES item_cardapio (id) ON DELETE CASCADE
);

CREATE INDEX idx_item_cardapio_imagem_item_cardapio_id ON item_cardapio_imagem (item_cardapio_id);

--------------------------------------------------------------------
-------------- MESA --------------
--------------------------------------------------------------------
CREATE TABLE mesa
(
    id          INTEGER GENERATED ALWAYS AS IDENTITY,
    numero_mesa INTEGER NOT NULL,

    CONSTRAINT pk_mesa PRIMARY KEY (id)
);

CREATE TABLE sessao_mesa
(
    id                     INTEGER GENERATED ALWAYS AS IDENTITY,
    mesa_id                INTEGER      NOT NULL,
    token                  VARCHAR(255) NOT NULL,
    data_hora_inicio       TIMESTAMP    NOT NULL,
    data_hora_encerramento TIMESTAMP,

    CONSTRAINT pk_sessao_mesa PRIMARY KEY (id),
    CONSTRAINT fk_sessao_mesa_mesa FOREIGN KEY (mesa_id)
        REFERENCES mesa (id) ON DELETE RESTRICT,
    CONSTRAINT uq_sessao_mesa_token UNIQUE (token)
);

CREATE INDEX idx_sessao_mesa_mesa_id ON sessao_mesa (mesa_id);

CREATE TABLE participante_sessao
(
    id        INTEGER GENERATED ALWAYS AS IDENTITY,
    sessao_id INTEGER      NOT NULL,
    token     VARCHAR(255) NOT NULL,
    nome      VARCHAR(255) NOT NULL,

    CONSTRAINT pk_participante_sessao PRIMARY KEY (id),
    CONSTRAINT fk_participante_sessao_mesa FOREIGN KEY (sessao_id)
        REFERENCES sessao_mesa (id) ON DELETE CASCADE,
    CONSTRAINT uq_participante_sessao_token UNIQUE (token)
);

CREATE INDEX idx_participante_sessao_id ON participante_sessao (sessao_id);
CREATE INDEX idx_participante_sessao_token ON participante_sessao (token);

CREATE TABLE solicitacao_entrada_sessao
(
    id        				INTEGER GENERATED ALWAYS AS IDENTITY,
    sessao_id 				INTEGER      NOT NULL,
    token     				VARCHAR(255) NOT NULL,
    nome      				VARCHAR(255) NOT NULL,
    data_hora_solicitacao   TIMESTAMP    NOT null,

    CONSTRAINT pk_solicitacao_entrada_sessao PRIMARY KEY (id),
    CONSTRAINT fk_solicitacao_sessao_mesa FOREIGN KEY (sessao_id)
        REFERENCES sessao_mesa (id) ON DELETE CASCADE,
    CONSTRAINT uq_solicitacao_usuario_token UNIQUE (token)
);

CREATE INDEX idx_solicitacao_entrada_sessao_id ON solicitacao_entrada_sessao(sessao_id);
CREATE INDEX idx_solicitacao_entrada_sessao_token ON solicitacao_entrada_sessao(token);
