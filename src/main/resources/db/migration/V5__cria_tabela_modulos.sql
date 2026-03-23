CREATE TABLE modulos (
    id       BIGSERIAL   PRIMARY KEY,
    nome     VARCHAR(50) NOT NULL,
    ordem    INT         NOT NULL,
    capa     VARCHAR(500),
    curso_id BIGINT      NOT NULL REFERENCES cursos(id) ON DELETE CASCADE
);
