CREATE TABLE usuarios (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    cpf         VARCHAR(14)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    senha       VARCHAR(255) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL,
    status      VARCHAR(10)  NOT NULL DEFAULT 'INATIVO',
    foto_perfil VARCHAR(500),
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW()
);
