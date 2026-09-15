CREATE TABLE clientes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL,
    email VARCHAR(180) NOT NULL,
    data_cadastro DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_clientes_cpf UNIQUE (cpf),
    CONSTRAINT uk_clientes_email UNIQUE (email)
);
