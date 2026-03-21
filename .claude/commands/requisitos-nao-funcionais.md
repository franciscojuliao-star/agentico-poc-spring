# Requisitos Não Funcionais — PoC LLM UFC

- **RNF01** – Spring Boot 3.4 com Java 21 (virtual threads habilitados via Tomcat)
- **RNF02** – Autenticação stateless com JWT: access token (curta duração) + refresh token (longa duração)
- **RNF03** – Documentação automática via SpringDoc OpenAPI 3 com suporte a Bearer JWT no Swagger UI
- **RNF04** – Banco PostgreSQL 16+ com controle de schema via Flyway (migrations versionadas)
- **RNF05** – Upload de arquivos local com validação de MIME type real via Apache Tika (não só extensão)
- **RNF06** – Senhas armazenadas com BCrypt (fator de custo padrão Spring Security)
- **RNF07** – HTML do CKEditor sanitizado via OWASP Java HTML Sanitizer antes de persistir (proteção XSS)
- **RNF08** – Listagens de cursos paginadas (Spring Data Pageable)
- **RNF09** – Respostas da API padronizadas: `{ data, message, status }` com códigos HTTP corretos
- **RNF10** – Integração com OpenAI GPT-4o via Spring AI para geração de conteúdo e quiz
- **RNF11** – Extração de texto de PDFs via Apache PDFBox para alimentar o Spring AI
- **RNF12** – Mapeamento Entidade ↔ DTO via MapStruct (sem mapeamento manual)
- **RNF13** – Envio de e-mails transacionais via Spring Mail (recuperação de senha)
