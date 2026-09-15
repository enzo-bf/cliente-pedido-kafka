CREATE TABLE historico_pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    status_anterior VARCHAR(30),
    status_novo VARCHAR(30) NOT NULL,
    data_hora DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_historico_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
);

CREATE INDEX idx_historico_pedido ON historico_pedido (pedido_id);
