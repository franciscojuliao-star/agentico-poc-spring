CREATE TABLE provas (
    id                         BIGSERIAL PRIMARY KEY,
    modulo_id                  BIGINT    NOT NULL UNIQUE REFERENCES modulos(id) ON DELETE CASCADE,
    mostrar_respostas_erradas  BOOLEAN   NOT NULL DEFAULT FALSE,
    mostrar_respostas_corretas BOOLEAN   NOT NULL DEFAULT FALSE,
    mostrar_valores            BOOLEAN   NOT NULL DEFAULT FALSE
);

CREATE TABLE perguntas (
    id        BIGSERIAL PRIMARY KEY,
    enunciado TEXT      NOT NULL,
    pontos    INT       NOT NULL DEFAULT 1,
    ordem     INT       NOT NULL,
    prova_id  BIGINT    NOT NULL REFERENCES provas(id) ON DELETE CASCADE
);

CREATE TABLE alternativas (
    id          BIGSERIAL PRIMARY KEY,
    texto       TEXT      NOT NULL,
    correta     BOOLEAN   NOT NULL DEFAULT FALSE,
    pergunta_id BIGINT    NOT NULL REFERENCES perguntas(id) ON DELETE CASCADE
);
