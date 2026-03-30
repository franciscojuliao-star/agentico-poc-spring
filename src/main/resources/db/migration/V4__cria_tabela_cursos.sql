CREATE TABLE cursos (
    id              BIGSERIAL    PRIMARY KEY,
    titulo          VARCHAR(255) NOT NULL,
    categoria       VARCHAR(100) NOT NULL,
    descricao       TEXT         NOT NULL,
    carga_horaria   VARCHAR(20)  NOT NULL,
    capa            VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'RASCUNHO',
    requer_endereco BOOLEAN      NOT NULL DEFAULT FALSE,
    requer_genero   BOOLEAN      NOT NULL DEFAULT FALSE,
    requer_idade    BOOLEAN      NOT NULL DEFAULT FALSE,
    professor_id    BIGINT       NOT NULL REFERENCES usuarios(id),
    criado_em       TIMESTAMP    NOT NULL DEFAULT NOW()
);
