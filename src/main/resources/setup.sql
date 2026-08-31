--------------------------------------------------------------------
-------------- CORE --------------
--------------------------------------------------------------------
CREATE TABLE tenant
(
    id   INTEGER GENERATED ALWAYS AS IDENTITY,
    nome VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,

    CONSTRAINT pk_tenant PRIMARY KEY (id)
);

CREATE TABLE estabelecimento
(
    id        INTEGER GENERATED ALWAYS AS IDENTITY,
    tenant_id INTEGER      NOT NULL,
    nome      VARCHAR(255) NOT null,

    CONSTRAINT pk_estabelecimento PRIMARY KEY (id),
    CONSTRAINT fk_estabelecimento_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenant (id)
);

CREATE INDEX idx_estabelecimento_tenant
    ON estabelecimento (tenant_id);

--------------------------------------------------------------------
-------------- CARDAPIO --------------
--------------------------------------------------------------------
CREATE TABLE menu
(
    id            INTEGER GENERATED ALWAYS AS IDENTITY,
    estabelecimento_id INTEGER NOT NULL,

    CONSTRAINT pk_menu PRIMARY KEY (id),
    CONSTRAINT fk_menu_estabelecimento
        FOREIGN KEY (estabelecimento_id)
            REFERENCES estabelecimento (id)
);

CREATE INDEX idx_menu_estabelecimento
    ON menu (estabelecimento_id);

CREATE TABLE menu_secao
(
    id     INTEGER GENERATED ALWAYS AS IDENTITY,
    menu_id           INTEGER NOT NULL,
    posicao_ordenacao INTEGER,

    CONSTRAINT pk_menu_secao PRIMARY KEY (id),
    CONSTRAINT fk_menu_secao_menu
        FOREIGN KEY (menu_id)
            REFERENCES menu (id)
);

CREATE INDEX idx_menu_secao_menu
    ON menu_secao (menu_id);


CREATE TABLE menu_item
(
    id      INTEGER GENERATED ALWAYS AS IDENTITY,
    menu_secao_id     INTEGER        NOT NULL,
    nome              VARCHAR(255)   NOT NULL,
    descricao         TEXT,
    preco             NUMERIC(10, 2) NOT NULL,
    posicao_ordenacao INTEGER,
    status            VARCHAR(30) NOT NULL,

    CONSTRAINT pk_menu_item PRIMARY KEY (id),
    CONSTRAINT fk_menu_item_menu_secao
        FOREIGN KEY (menu_secao_id)
            REFERENCES menu_secao (id)
);

CREATE INDEX idx_menu_item_menu_secao
    ON menu_item (menu_secao_id);


CREATE TABLE imagem_menu_item
(
    id INTEGER GENERATED ALWAYS AS IDENTITY,
    menu_item_id        INTEGER NOT NULL,
    url                 TEXT    NOT NULL,
    posicao_ordenacao   INTEGER,

    CONSTRAINT pk_imagem_menu_item PRIMARY KEY (id),
    CONSTRAINT fk_imagem_menu_item_menu_item
        FOREIGN KEY (menu_item_id)
            REFERENCES menu_item (id)
);

CREATE INDEX idx_imagem_menu_item_menu_item
    ON imagem_menu_item (menu_item_id);

--------------------------------------------------------------------
-------------- MESA --------------
--------------------------------------------------------------------
CREATE TABLE mesa
(
    id                 INTEGER GENERATED ALWAYS AS IDENTITY,
    numero_mesa        INTEGER NOT NULL,
    estabelecimento_id INTEGER NOT NULL,

    CONSTRAINT pk_mesa PRIMARY KEY (id),
    CONSTRAINT fk_menu_estabelecimento
        FOREIGN KEY (estabelecimento_id)
            REFERENCES estabelecimento (id)
);

CREATE INDEX idx_mesa_estabelecimento
    ON mesa (estabelecimento_id);

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

--------------------------------------------------------------------
-------------- OPERACIONAL --------------
--------------------------------------------------------------------

CREATE TABLE solicitacao_entrada_sessao
(
    id                    INTEGER GENERATED ALWAYS AS IDENTITY,
    sessao_id             INTEGER      NOT NULL,
    token                 VARCHAR(255) NOT NULL,
    nome                  VARCHAR(255) NOT NULL,
    data_hora_solicitacao TIMESTAMP    NOT null,

    CONSTRAINT pk_solicitacao_entrada_sessao PRIMARY KEY (id),
    CONSTRAINT fk_solicitacao_sessao_mesa FOREIGN KEY (sessao_id)
        REFERENCES sessao_mesa (id) ON DELETE CASCADE,
    CONSTRAINT uq_solicitacao_usuario_token UNIQUE (token)
);

CREATE INDEX idx_solicitacao_entrada_sessao_id ON solicitacao_entrada_sessao (sessao_id);
CREATE INDEX idx_solicitacao_entrada_sessao_token ON solicitacao_entrada_sessao (token);

CREATE TABLE pedido (
    id                      INTEGER GENERATED ALWAYS AS IDENTITY,
    participante_sessao_id  INTEGER NOT NULL,
    menu_item_id            INTEGER NOT NULL,
    valor_unitario          NUMERIC(10,2) NOT NULL,
    quantidade              INTEGER NOT NULL,
    status                  VARCHAR(30) NOT NULL,
    data_hora_pedido        TIMESTAMP NOT NULL,
    data_hora_cancelamento  TIMESTAMP,

    CONSTRAINT pk_pedido PRIMARY KEY (id),
    CONSTRAINT fk_pedido_participante_sessao FOREIGN KEY (participante_sessao_id)
        REFERENCES participante_sessao (id),
    CONSTRAINT fk_pedido_menu_item FOREIGN KEY (menu_item_id)
        REFERENCES menu_item (id),
    CONSTRAINT ck_pedido_quantidade_positiva CHECK (quantidade > 0),
    CONSTRAINT ck_pedido_valor_unitario_positivo CHECK (valor_unitario > 0)
);

CREATE INDEX idx_pedido_participante_sessao_id ON pedido (participante_sessao_id);
CREATE INDEX idx_pedido_menu_item_id ON pedido (menu_item_id);
CREATE INDEX idx_pedido_status ON pedido (status);