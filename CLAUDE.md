# PoC LLM UFC — Briefing do Projeto

## O que é
Plataforma LMS (Learning Management System) desenvolvida para a UFC,
inspirada na Anthropic Academy. Backend API REST em Spring Boot com IA
embarcada (Spring AI + OpenAI GPT-4o) para geração de conteúdo de aulas
e quizzes de módulos.

## Perfis de Usuário
- ADMIN     → pré-cadastrado no sistema; ativa/desativa contas
- PROFESSOR → cria e gerencia cursos, módulos, aulas e provas
- ALUNO     → se inscreve e consome cursos (Fase 2 — não implementado ainda)

## Stack
Java 21 · Spring Boot 3.4 · PostgreSQL 16 · Flyway · JWT (JJWT)
Spring AI · OpenAI GPT-4o · Apache PDFBox · Apache Tika
OWASP HTML Sanitizer · MapStruct · Lombok · Spring Mail · Maven

## Estrutura de Branches (GitFlow)
- main          → produção (merge via PR aprovado)
- development   → integração contínua (base de todo trabalho)
- feature/xxx   → nova funcionalidade (base: development)
- bugfix/xxx    → correção de bug (base: development)
- hotfix/xxx    → correção urgente em produção (base: main)

## Modelo de Dados Principal
```
Usuario (1) ──> (N) Curso
Curso   (1) ──> (N) Modulo
Modulo  (1) ──> (N) Aula
Modulo  (1) ──> (0..1) Prova
Prova   (1) ──> (N) Pergunta
Pergunta(1) ──> (N) Alternativa
Usuario (1) ──> (N) TokenRecuperacaoSenha
```

## Convenções de Código
- Idioma: português para nomes de entidades, campos, métodos e variáveis de domínio
- Pacote raiz: `br.ufc.llm`
- Estrutura de pacotes por domínio:
  ```
  br.ufc.llm.usuario / curso / modulo / aula / prova / shared
  Cada domínio: controller · service · repository · domain · dto · exception
  ```
- DTOs separados: `XxxRequest` (entrada) e `XxxResponse` (saída)
- Resposta padrão da API: `{ "data": {}, "message": "", "status": 200 }`
- Commits em português no imperativo: `"Adiciona endpoint de criação de curso"`
- **Testes escritos ANTES da implementação (TDD)**
- **Quando a IA errar: descreva o erro, não corrija manualmente**

## Fase Atual
Fase 1 — Perfil Professor: auth · perfil · cursos · módulos · aulas · provas · Spring AI

## Comandos Disponíveis
```
/requisitos-funcionais      → todos os RFs da Fase 1
/regras-negocio             → todas as RNs do sistema
/requisitos-nao-funcionais  → RNFs e justificativas técnicas
/arquitetura                → entidades, tabelas SQL, endpoints REST
/user-stories               → histórias de usuário por perfil
```
