# Controle de Gastos Pessoais

Aplicação web simples para registrar receitas e despesas por categoria, acompanhar as movimentações do mês e consultar receitas, despesas e saldo. O backend é uma API REST Java e a interface estática é servida pelo Spring Boot.

## Tecnologias

- Java 21 e Spring Boot
- Spring Web MVC, Spring Data JPA e Hibernate
- PostgreSQL
- Bean Validation e DTOs
- Spring Security com HTTP Basic
- springdoc-openapi / Swagger UI
- HTML, CSS e JavaScript

## Funcionalidades

- CRUD de categorias, com proteção contra exclusão de categorias vinculadas a transações
- CRUD de transações, validação de valores monetários e associação com categoria
- Listagem filtrada por mês/ano e resumo de receitas, despesas e saldo
- Interface em `/` e documentação interativa da API em `/swagger-ui/index.html`
- API e interface protegidas por autenticação HTTP Basic

## Rodar localmente

Requisitos: Java 21, Docker ou PostgreSQL local com um banco chamado `controle_gastos`.

Configure as variáveis de ambiente antes de iniciar. No PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/controle_gastos"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "sua-senha-local"
$env:APP_SECURITY_USERNAME = "demo"
$env:APP_SECURITY_PASSWORD = "escolha-uma-senha-local"
.\mvnw.cmd spring-boot:run
```

Abra `http://localhost:8080` e autentique com o usuário e a senha definidos acima. Nunca use sua senha pessoal ou dados financeiros reais em uma instância pública.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes de service usam Mockito; os testes MVC verificam autenticação, validação e resposta do resumo. O teste de contexto usa H2 isolado e não requer PostgreSQL.

## Decisões técnicas

- `BigDecimal` com precisão e escala definidas evita erros de ponto flutuante em valores monetários.
- DTOs separam o contrato da API das entidades JPA, evitando expor IDs arbitrários e proxies LAZY.
- Services concentram regras de negócio; `@EntityGraph` carrega categorias junto das transações nas listagens, evitando consultas N+1.
- HTTP Basic exige credenciais externas ao código. Defina `APP_SECURITY_USERNAME` e `APP_SECURITY_PASSWORD` no ambiente de execução; a senha é armazenada em memória usando BCrypt. Em produção, use HTTPS e dados fictícios.
- O schema permanece com `spring.jpa.hibernate.ddl-auto=update` para facilitar a demonstração local. Para produção, migrar a criação/evolução do schema para Flyway é um próximo passo.

## Docker e publicação

O `Dockerfile` constrói a aplicação com Java 21. Para executar localmente:

```powershell
docker build -t controle-gastos .
docker run --rm -p 8080:8080 `
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/controle_gastos" `
  -e SPRING_DATASOURCE_USERNAME="postgres" `
  -e SPRING_DATASOURCE_PASSWORD="sua-senha-local" `
  -e APP_SECURITY_USERNAME="demo" `
  -e APP_SECURITY_PASSWORD="escolha-uma-senha-local" `
  controle-gastos
```

Para publicar, configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `APP_SECURITY_USERNAME` e `APP_SECURITY_PASSWORD` como variáveis/segredos no provedor. A porta usa automaticamente a variável `PORT`, com fallback para `8080`. Em bancos gerenciados, use a URL JDBC e SSL exigidas pelo provedor.

Não há demo pública configurada neste repositório ainda. Ao publicar, use somente dados fictícios e crie credenciais descartáveis para demonstração; não publique senhas reais no README. Planos gratuitos de hospedagem podem suspender o serviço por inatividade e demorar para responder à primeira requisição. Confirme as regras e a política de retenção do banco escolhido antes de compartilhar o link.

## Próximas melhorias

- Substituir `ddl-auto=update` por migrações Flyway.
- Adicionar deploy contínuo e capturas de tela da aplicação publicada.
- Avaliar paginação para listagens maiores.
