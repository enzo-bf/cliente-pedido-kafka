CREATE TABLE pedidos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(15, 2) NOT NULL,
    desconto DECIMAL(5, 2) NOT NULL,
    valor_final DECIMAL(15, 2) NOT NULL,
    data_criacao DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL,
    cliente_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT ck_pedidos_valor CHECK (valor > 0),
    CONSTRAINT ck_pedidos_desconto CHECK (desconto >= 0 AND desconto <= 20)
);

CREATE INDEX idx_pedidos_cliente ON pedidos (cliente_id);
CREATE INDEX idx_pedidos_status ON pedidos (status);
