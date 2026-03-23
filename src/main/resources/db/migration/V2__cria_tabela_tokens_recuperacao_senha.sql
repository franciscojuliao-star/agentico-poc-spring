CREATE TABLE tokens_recuperacao_senha (
    id          BIGSERIAL    PRIMARY KEY,
    token       VARCHAR(36)  NOT NULL UNIQUE,
    usuario_id  BIGINT       NOT NULL REFERENCES usuarios(id),
    expirado_em TIMESTAMP    NOT NULL,
    usado       BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW()
);
