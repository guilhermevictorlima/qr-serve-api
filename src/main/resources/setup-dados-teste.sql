-- Dados de teste para o schema de setup.sql.
-- Pressupõe banco recém-criado (IDs identity começam em 1).
-- As sessões são inseridas por último.

--------------------------------------------------------------------
-------------- CORE --------------
--------------------------------------------------------------------
INSERT INTO tenant (nome, slug)
VALUES ('Tenant Teste', 'tenant-teste');

INSERT INTO estabelecimento (tenant_id, nome)
VALUES (1, 'Restaurante Teste');

--------------------------------------------------------------------
-------------- CARDAPIO --------------
--------------------------------------------------------------------
INSERT INTO menu (estabelecimento_id)
VALUES (1);

INSERT INTO menu_secao (menu_id, posicao_ordenacao)
VALUES (1, 1),  -- id 1: Pratos
       (1, 2);  -- id 2: Bebidas

INSERT INTO menu_item (menu_secao_id, nome, descricao, preco, posicao_ordenacao, status)
VALUES (1, 'Hambúrguer', 'Pão, carne 180g, queijo e salada', 32.90, 1, 'DISPONIVEL'),
       (1, 'Batata Frita', 'Porção individual', 18.00, 2, 'DISPONIVEL'),
       (1, 'Salada Caesar', 'Indisponível no momento', 27.50, 3, 'INDISPONIVEL'),
       (2, 'Refrigerante', 'Lata 350ml', 6.50, 1, 'DISPONIVEL'),
       (2, 'Suco de Laranja', 'Copo 400ml', 9.00, 2, 'DISPONIVEL');

INSERT INTO imagem_menu_item (menu_item_id, url, posicao_ordenacao)
VALUES (1, 'https://example.com/imagens/hamburguer.jpg', 1),
       (2, 'https://example.com/imagens/batata-frita.jpg', 1);

--------------------------------------------------------------------
-------------- MESA --------------
--------------------------------------------------------------------
INSERT INTO mesa (numero_mesa, estabelecimento_id)
VALUES (1, 1),  -- id 1: livre (sem sessão)
       (2, 1),  -- id 2: sessão recém-aberta
       (3, 1);  -- id 3: sessão aberta há 3 minutos

--------------------------------------------------------------------
-------------- SESSOES (por último) --------------
--------------------------------------------------------------------
INSERT INTO sessao_mesa (mesa_id, token, data_hora_inicio, data_hora_encerramento)
VALUES (2, 'sessao-mesa-2-token', NOW(), NULL),
       (3, 'sessao-mesa-3-token', NOW() - INTERVAL '3 minutes', NULL);

INSERT INTO participante_sessao (sessao_id, token, nome)
VALUES (1, 'participante-mesa-2-ana-token', 'Ana'),
       (2, 'participante-mesa-3-bruno-token', 'Bruno'),
       (2, 'participante-mesa-3-carla-token', 'Carla');

INSERT INTO solicitacao_entrada_sessao (sessao_id, token, nome, data_hora_solicitacao)
VALUES (2, 'solicitacao-mesa-3-diego-token', 'Diego', NOW() - INTERVAL '1 minute');

INSERT INTO pedido (participante_sessao_id, menu_item_id, valor_unitario, quantidade, status, data_hora_pedido)
VALUES (2, 1, 32.90, 1, 'PEDIDO_REALIZADO', NOW() - INTERVAL '2 minutes'),
       (3, 4, 6.50, 2, 'PEDIDO_REALIZADO', NOW() - INTERVAL '1 minute');
