package br.com.spacovip.salao.controller;

import br.com.spacovip.salao.dto.cliente.ClienteRequestDTO;
import br.com.spacovip.salao.dto.cliente.ClienteResponseDTO;
import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.ConflitoUnicidadeException;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@Import(ClienteControllerTest.TestConfig.class)
class ClienteControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDate DATA_NASCIMENTO_FIXA = LocalDate.of(1990, 5, 10);
    private static final LocalDateTime DATA_CADASTRO_FIXA = LocalDateTime.of(2024, 1, 15, 10, 30);
    private static final LocalDateTime DATA_ATUALIZACAO_FIXA = LocalDateTime.of(2024, 1, 20, 14, 45);
    private static final UUID UUID_FIXO = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final String BASE_URL = "/spacovip/clientes";

    private ClienteRequestDTO criarRequestValido() {
        return new ClienteRequestDTO(
                "Maria Silva",
                "maria@email.com",
                "11987654321",
                DATA_NASCIMENTO_FIXA,
                Sexo.FEMININO
        );
    }

    private ClienteResponseDTO criarResponseValido(UUID id) {
        return new ClienteResponseDTO(
                id,
                "Maria Silva",
                "maria@email.com",
                "11987654321",
                DATA_NASCIMENTO_FIXA,
                Status.ATIVO,
                Sexo.FEMININO,
                DATA_CADASTRO_FIXA,
                DATA_ATUALIZACAO_FIXA
        );
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Nested
    @DisplayName("POST /spacovip/clientes - Cadastrar Cliente")
    class CadastrarCliente {

        @Test
        @DisplayName("Deve retornar 201 Created com cliente criado")
        void deveRetornar201ComClienteCriado() throws Exception {
            ClienteRequestDTO request = criarRequestValido();
            ClienteResponseDTO response = criarResponseValido(UUID_FIXO);

            when(service.cadastrar(any(ClienteRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(UUID_FIXO.toString()))
                    .andExpect(jsonPath("$.nome").value("Maria Silva"))
                    .andExpect(jsonPath("$.telefone").value("11987654321"))
                    .andExpect(jsonPath("$.status").value("ATIVO"))
                    .andExpect(jsonPath("$.sexo").value("FEMININO"));

            verify(service, times(1)).cadastrar(any(ClienteRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando JSON inválido")
        void deveRetornar400QuandoJsonInvalido() throws Exception {
            String jsonInvalido = "{ \"nome\": \"Maria\", \"email\": \"invalido\", \"telefone\": \"123\", \"dataNascimento\": \"10/05/1990\", \"sexo\": \"FEMININO\" }";

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonInvalido))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(service, never()).cadastrar(any(ClienteRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict quando email já existe")
        void deveRetornar409QuandoEmailJaExiste() throws Exception {
            ClienteRequestDTO request = criarRequestValido();

            when(service.cadastrar(any(ClienteRequestDTO.class)))
                    .thenThrow(new ConflitoUnicidadeException("E-mail indisponível para uso"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.erro").value("E-mail indisponível para uso"));

            verify(service, times(1)).cadastrar(any(ClienteRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict quando telefone já existe")
        void deveRetornar409QuandoTelefoneJaExiste() throws Exception {
            ClienteRequestDTO request = criarRequestValido();

            when(service.cadastrar(any(ClienteRequestDTO.class)))
                    .thenThrow(new ConflitoUnicidadeException("Telefone indisponível para uso"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.erro").value("Telefone indisponível para uso"));
        }
    }

    @Nested
    @DisplayName("GET /spacovip/clientes - Listar Clientes")
    class ListarClientes {

        @Test
        @DisplayName("Deve retornar 200 OK com página de clientes")
        void deveRetornar200ComPaginaDeClientes() throws Exception {
            ClienteResponseDTO response = criarResponseValido(UUID_FIXO);
            Page<ClienteResponseDTO> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

            when(service.buscarTodosClientes(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "nome,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(UUID_FIXO.toString()))
                    .andExpect(jsonPath("$.content[0].nome").value("Maria Silva"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(10));

            verify(service, times(1)).buscarTodosClientes(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar 200 OK com página vazia quando não há clientes")
        void deveRetornar200ComPaginaVazia() throws Exception {
            Page<ClienteResponseDTO> pageVazia = Page.empty();
            when(service.buscarTodosClientes(any(Pageable.class))).thenReturn(pageVazia);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Deve aplicar paginação e ordenação personalizadas")
        void deveAplicarPaginacaoEOrdenacaoPersonalizadas() throws Exception {
            Page<ClienteResponseDTO> page = Page.empty();
            when(service.buscarTodosClientes(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .param("page", "1")
                            .param("size", "5")
                            .param("sort", "id,desc"))
                    .andExpect(status().isOk());

            verify(service, times(1)).buscarTodosClientes(argThat(p ->
                    p.getPageNumber() == 1 &&
                    p.getPageSize() == 5 &&
                    p.getSort().toString().contains("id: DESC")
            ));
        }
    }

    @Nested
    @DisplayName("GET /spacovip/clientes/{id} - Buscar Cliente por ID")
    class BuscarClientePorId {

        @Test
        @DisplayName("Deve retornar 200 OK com cliente encontrado")
        void deveRetornar200ComClienteEncontrado() throws Exception {
            ClienteResponseDTO response = criarResponseValido(UUID_FIXO);
            when(service.buscarPorId(UUID_FIXO)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/" + UUID_FIXO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(UUID_FIXO.toString()))
                    .andExpect(jsonPath("$.nome").value("Maria Silva"))
                    .andExpect(jsonPath("$.status").value("ATIVO"));

            verify(service, times(1)).buscarPorId(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando cliente não existe")
        void deveRetornar404QuandoClienteNaoExiste() throws Exception {
            UUID idInexistente = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            when(service.buscarPorId(idInexistente))
                    .thenThrow(new ResourceNotFoundException("Cliente não encontrado com o ID: " + idInexistente));

            mockMvc.perform(get(BASE_URL + "/" + idInexistente))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.erro").value("Cliente não encontrado com o ID: " + idInexistente));
        }
    }

    @Nested
    @DisplayName("PUT /spacovip/clientes/{id} - Atualizar Cliente")
    class AtualizarCliente {

        @Test
        @DisplayName("Deve retornar 200 OK com cliente atualizado")
        void deveRetornar200ComClienteAtualizado() throws Exception {
            ClienteRequestDTO request = new ClienteRequestDTO(
                    "Maria Santos",
                    "maria.novo@email.com",
                    "11999999999",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.FEMININO
            );
            ClienteResponseDTO response = new ClienteResponseDTO(
                    UUID_FIXO, "Maria Santos", "maria.novo@email.com", "11999999999", DATA_NASCIMENTO_FIXA, Status.ATIVO, Sexo.FEMININO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA
            );

            when(service.atualizar(eq(UUID_FIXO), any(ClienteRequestDTO.class))).thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/" + UUID_FIXO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(UUID_FIXO.toString()))
                    .andExpect(jsonPath("$.nome").value("Maria Santos"))
                    .andExpect(jsonPath("$.telefone").value("11999999999"));

            verify(service, times(1)).atualizar(eq(UUID_FIXO), any(ClienteRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando ID não existe")
        void deveRetornar404QuandoIdNaoExisteAoAtualizar() throws Exception {
            ClienteRequestDTO request = criarRequestValido();
            UUID idInexistente = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

            when(service.atualizar(eq(idInexistente), any(ClienteRequestDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Cliente não encontrado com o ID: " + idInexistente));

            mockMvc.perform(put(BASE_URL + "/" + idInexistente)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando JSON inválido")
        void deveRetornar400QuandoJsonInvalidoAoAtualizar() throws Exception {
            String jsonInvalido = "{ \"nome\": \"\", \"email\": \"invalido\", \"telefone\": \"123\", \"dataNascimento\": \"10/05/1990\", \"sexo\": \"FEMININO\" }";

            mockMvc.perform(put(BASE_URL + "/" + UUID_FIXO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonInvalido))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict quando email de outro cliente")
        void deveRetornar409QuandoEmailDeOutroCliente() throws Exception {
            ClienteRequestDTO request = criarRequestValido();

            when(service.atualizar(eq(UUID_FIXO), any(ClienteRequestDTO.class)))
                    .thenThrow(new ConflitoUnicidadeException("Este e-mail não está disponivel."));

            mockMvc.perform(put(BASE_URL + "/" + UUID_FIXO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict quando telefone de outro cliente")
        void deveRetornar409QuandoTelefoneDeOutroCliente() throws Exception {
            ClienteRequestDTO request = criarRequestValido();

            when(service.atualizar(eq(UUID_FIXO), any(ClienteRequestDTO.class)))
                    .thenThrow(new ConflitoUnicidadeException("Este telefone não está disponivel."));

            mockMvc.perform(put(BASE_URL + "/" + UUID_FIXO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }
    }

    @Nested
    @DisplayName("PUT /spacovip/clientes/{id}/desativar - Desativar Cliente")
    class DesativarCliente {

        @Test
        @DisplayName("Deve retornar 204 No Content ao desativar")
        void deveRetornar204AoDesativar() throws Exception {
            doNothing().when(service).desativar(UUID_FIXO);

            mockMvc.perform(put(BASE_URL + "/" + UUID_FIXO + "/desativar"))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).desativar(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando ID não existe")
        void deveRetornar404QuandoIdNaoExisteAoDesativar() throws Exception {
            UUID idInexistente = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            doThrow(new ResourceNotFoundException("Cliente não encontrado com o ID: " + idInexistente))
                    .when(service).desativar(idInexistente);

            mockMvc.perform(put(BASE_URL + "/" + idInexistente + "/desativar"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("DELETE /spacovip/clientes/{id} - Excluir Cliente")
    class ExcluirCliente {

        @Test
        @DisplayName("Deve retornar 204 No Content ao excluir")
        void deveRetornar204AoExcluir() throws Exception {
            doNothing().when(service).excluir(UUID_FIXO);

            mockMvc.perform(delete(BASE_URL + "/" + UUID_FIXO))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).excluir(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando ID não existe")
        void deveRetornar404QuandoIdNaoExisteAoExcluir() throws Exception {
            UUID idInexistente = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            doThrow(new ResourceNotFoundException("Cliente não encontrado para exclusão com o ID: " + idInexistente))
                    .when(service).excluir(idInexistente);

            mockMvc.perform(delete(BASE_URL + "/" + idInexistente))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("Casos de Borda e Validações Extras")
    class CasosDeBorda {

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando UUID malformado no path")
        void deveRetornar400QuandoUuidMalformado() throws Exception {
            mockMvc.perform(get(BASE_URL + "/uuid-invalido"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.erro").exists());
        }

        @Test
        @DisplayName("Deve retornar 415 Unsupported Media Type quando Content-Type inválido")
        void deveRetornar415QuandoContentTypeInvalido() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("não é json"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.status").value(415))
                    .andExpect(jsonPath("$.erro").exists());
        }

        @Test
        @DisplayName("Deve aceitar data no formato dd/MM/yyyy no JSON")
        void deveAceitarDataFormatoBrasileiro() throws Exception {
            String jsonComDataBrasileira = """
                    {
                        "nome": "João Silva",
                        "email": "joao@email.com",
                        "telefone": "11912345678",
                        "dataNascimento": "10/05/1990",
                        "sexo": "MASCULINO"
                    }
                    """;

            ClienteResponseDTO response = new ClienteResponseDTO(
                    UUID_FIXO, "João Silva", "joao@email.com", "11912345678", DATA_NASCIMENTO_FIXA, Status.ATIVO, Sexo.MASCULINO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA
            );
            when(service.cadastrar(any(ClienteRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonComDataBrasileira))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nome").value("João Silva"))
                    .andExpect(jsonPath("$.sexo").value("MASCULINO"));
        }

        @Test
        @DisplayName("Deve retornar 405 Method Not Allowed para método não suportado")
        void deveRetornar405ParaMetodoNaoSuportado() throws Exception {
            mockMvc.perform(patch(BASE_URL))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.status").value(405));
        }
    }
}