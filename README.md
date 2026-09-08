# 💇 Salão API

**Uma API RESTful completa e escalável para gerenciamento de um salão de beleza.**

Este projeto foi construído do zero, focado em aprendizado para novas annotations disponiveis no Spring 4.1.0.
Inicialmente, criarei uma estrutura CRUD como base, para então ir aprimorando e trazendo mais usabilidade no mundo real.

## 🚀 Tecnologias e Ferramentas

O backend foi desenvolvido utilizando as tecnologias mais robustas do mercado corporativo:

*   **Java 25**
*   **Spring Boot** (Spring Web, Spring Data JPA, Spring Validation).
*   **PostgreSQL**
*   **Hibernate**
*   **Lombok**
*   **SLF4J**
*   **Maven**

## 🏗️ Arquitetura e Decisões de Design

A API foi estruturada seguindo os princípios de **Clean Architecture** (Arquitetura em Camadas) e **SOLID**, garantindo baixo acoplamento e alta coesão.

### Destaques Técnicos:
*   **Separação de Responsabilidades:** Utilização do padrão DTO (Data Transfer Object) implementado nativamente com `records` do Java moderno.
*   **Mapeamento de Dados Seguro:** Implementação do padrão *Mapper* manual, isolando a conversão de objetos (RequestDTO ↔ Entity ↔ ResponseDTO) para blindar as entidades do banco de dados.
*   **Validação Robusta:** Utilização do *Jakarta Validation* (`@NotBlank`, `@Pattern`, `@Positive`) nas portas de entrada (Controllers) para evitar processamento de dados inconsistentes.
*   **Segurança e Consistência:**
    *   Uso de **UUIDs** (Universally Unique Identifier) como chave primária para todas as entidades, dificultando a predição de dados e enumeração.
    *   Proteção contra corrupção de enums no banco usando `@Enumerated(EnumType.STRING)`.
*   **Automatização de Processos:**
    *   Uso do ciclo de vida do JPA (`@PrePersist` e `@PreUpdate`) para gerenciamento de datas de cadastro e atualização, isolando essas informações dos usuários e do frontend.
    *   Tarefas assíncronas configuradas com `@Scheduled` para inativação automática de clientes que não tiverem novos agendamentos feitos num período de 6 meses do ultimo agendamento, ou, caso não tenha agendamento, da data de criação. Além disso, caso o cliente esteja há 6 meses inativado, ele será excluido do banco de dados do Salão. **Ainda vou implementar na service.**
*   **Relacionamento Flexível:** Implementação de relacionamento bidirecional Muitos-Para-Muitos (`@ManyToMany`) entre Profissionais e Serviços, permitindo escabilidade no portfólio do salão.

## 📦 Estrutura de Domínio

O sistema gerencia o núcleo do salão de beleza focado nas seguintes entidades:
*   **Cliente:** Gestão de perfis e histórico (Inicialmente concluída).
*   **Profissional:** Gestão de equipe e especialidades (Em desenvolvimento).
*   **Serviço:** Catálogo de opções com precificação e duração (Em desenvolvimento).
*   **Agendamento:** Orquestração do atendimento (Em desenvolvimento).

## 🛠️ Como rodar a aplicação localmente

### Pré-requisitos
*   JDK 25 ou superior.
*   Maven.
*   PostgreSQL em execução (localmente ou via Docker).

### Passos

1. Clone o repositório:
   ```bash
   git clone [https://github.com/SEU_USUARIO/spaco-vip-api.git](https://github.com/SEU_USUARIO/spaco-vip-api.git)
   ```
2. Acesse a pasta do projeto:
    ```bash
   cd spaco-vip-api
    ```
3. Execute o projeto com o Maven:
    ```bash
   mvn spring-boot:run 
   ```
A API estará disponível por padrão na porta 8080.

## 📈 Próximos Passos (Backlog)

[✅] Aprofundar o estudo no ciclo de vida do JPA (@PrePersist e @PreUpdate).

[ ] Implementar a lógica da rotina de inativação automática na camada de Service usando @Scheduled.

[ ] Criar e mapear a entidade principal de Agendamento.

[✅] (Parcial) Implementar os Controllers para liberar os testes pelo Postman.

[ ] Integração futura com o Frontend (React).

Desenvolvido com curiosidade e foco por Ícaro.
