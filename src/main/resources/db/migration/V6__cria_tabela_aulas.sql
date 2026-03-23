CREATE TABLE aulas (
    id                 BIGSERIAL    PRIMARY KEY,
    nome               VARCHAR(255) NOT NULL,
    ordem              INT          NOT NULL,
    arquivo            VARCHAR(500),
    tipo_arquivo       VARCHAR(10),
    conteudo_ck_editor TEXT,
    conteudo_gerado    TEXT,
    modulo_id          BIGINT       NOT NULL REFERENCES modulos(id) ON DELETE CASCADE
);
