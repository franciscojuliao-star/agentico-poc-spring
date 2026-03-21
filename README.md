# PoC LLM UFC

Plataforma LMS (Learning Management System) desenvolvida para a Universidade Federal do Ceará,
inspirada na Anthropic Academy. Backend API REST em Spring Boot com IA embarcada (Spring AI + OpenAI GPT-4o)
para geração de conteúdo de aulas e quizzes de módulos.

## Pré-requisitos

- Java 21
- Maven
- PostgreSQL 16+

## Configuração do ambiente

Copie o arquivo de exemplo e preencha com seus valores:

```bash
cp .env.example .env
```

## Configuração do banco de dados

```bash
sudo service postgresql start

sudo -u postgres psql -c "CREATE USER poc_user WITH PASSWORD 'poc123';"
sudo -u postgres psql -c "CREATE DATABASE poc_llm_ufc OWNER poc_user;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE poc_llm_ufc TO poc_user;"
```

> As tabelas são criadas automaticamente pelo Flyway ao subir a aplicação.

## Comandos

### Rodar em desenvolvimento
```bash
./mvnw spring-boot:run
```
Sobe a aplicação direto do código fonte. Ideal para desenvolvimento — não gera jar.

---

### Rodar forçando recompilação completa
```bash
./mvnw clean spring-boot:run
```
Apaga os arquivos compilados anteriores e recompila tudo antes de subir.
Use quando uma alteração não for refletida com o comando anterior.

---

### Gerar o jar (executável para produção)
```bash
./mvnw clean package -DskipTests
```
Compila o projeto e gera o arquivo `target/poc-llm-ufc-0.0.1-SNAPSHOT.jar`.
O `clean` garante que o jar anterior seja substituído.
O `-DskipTests` pula os testes para agilizar — rode os testes antes separadamente.

---

### Rodar o jar gerado
```bash
java -jar target/poc-llm-ufc-0.0.1-SNAPSHOT.jar
```
Roda o jar sem precisar do Maven. Requer apenas Java 21 instalado e o banco configurado.

---

### Rodar apenas os testes
```bash
./mvnw test
```
Executa todos os testes sem subir a aplicação. Sempre rode antes de gerar o jar.

---

### Compilar sem subir
```bash
./mvnw compile
```
Apenas compila o código sem rodar. Útil para verificar erros de compilação rapidamente.

## Swagger UI

Com a aplicação rodando, acesse: `http://localhost:8080/swagger-ui.html`

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DB_URL` | URL do banco: `jdbc:postgresql://localhost:5432/poc_llm_ufc` |
| `DB_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave secreta para assinar os tokens JWT |
| `OPENAI_API_KEY` | Chave da API OpenAI (GPT-4o) |
| `MAIL_USERNAME` | E-mail para envio de recuperação de senha |
| `MAIL_PASSWORD` | Senha de app do Gmail |
| `UPLOAD_DIR` | Diretório de upload de arquivos (padrão: `uploads`) |

## Stack

Java 21 · Spring Boot 3.4 · PostgreSQL 16 · Flyway · JWT · Spring AI (OpenAI GPT-4o) · Maven