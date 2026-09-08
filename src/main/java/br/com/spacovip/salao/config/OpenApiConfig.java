package br.com.spacovip.salao.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI salaoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spaço Vip API")
                        .version("v1")
                        .description("""
                                API RESTful para gerenciamento de salão de beleza (Spaço Vip).
                                
                                ## Funcionalidades
                                * **Clientes** - Cadastro, listagem, busca, atualização, desativação e exclusão
                                * **Profissionais** - Gestão da equipe e especialidades (serviços vinculados)
                                * **Serviços** - Catálogo de serviços com preço e duração (em desenvolvimento)
                                * **Agendamentos** - Orquestração dos atendimentos (planejado)
                                
                                ## Tecnologias
                                * Java 25
                                * Spring Boot 4.1.0
                                * PostgreSQL + JPA/Hibernate
                                * Lombok, Validation, Actuator
                                
                                ## Autenticação
                                Atualmente a API é pública. Autenticação JWT será implementada em versão futura.
                                """)
                        .contact(new Contact()
                                .name("Ícaro")
                                .email("icaro_mota@hotmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desenvolvimento Local"),
                        new Server().url("https://api.spacovip.com.br").description("Produção")))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositório do Projeto")
                        .url("https://github.com/icaro-amf/salao"))
                .components(new Components());
    }
}