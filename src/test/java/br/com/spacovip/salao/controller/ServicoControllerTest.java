package br.com.spacovip.salao.controller;

import br.com.spacovip.salao.dto.servico.ServicoRequestDTO;
import br.com.spacovip.salao.dto.servico.ServicoResponseDTO;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.ConflitoUnicidadeException;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.service.ServicoService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServicoController.class)
@Import(ServicoControllerTest.TestConfig.class)
class ServicoControllerTest {

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
    private ServicoService service;

    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDateTime DATA_CADASTRO_FIXA = LocalDateTime.of(2024, 1, 15, 10, 30);
    private static final LocalDateTime DATA_ATUALIZACAO_FIXA = LocalDateTime.of(2024, 1, 20, 14, 45);
    private static final UUID UUID_FIXO = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final String BASE_URL = "/spacovip/servicos";

    private ServicoRequestDTO criarRequestValido() {
        return new ServicoRequestDTO(
                "Corte de Cabelo",
                "Corte masculino tradicional",
                30L,
                new BigDecimal("50.00")
        );
    }

    private ServicoResponseDTO criarResponseValido(UUID id, Status status) {
        return new ServicoResponseDTO(
                id,
                "Corte de Cabelo",
                "Corte masculino tradicional",
                30L,
                new BigDecimal("50.00"),
                status,
                DATA_CADASTRO_FIXA,
                DATA_ATUALIZACAO_FIXA,
                List.of()
        );
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Nested
    @DisplayName("POST /spacovip/servicos - Cadastrar Serviço")
    class CadastrarServico {

        @Test
        @DisplayName("Deve retornar 201 Created com serviço criado")
        void deveRetornar201ComServicoCriado() throws Exception {
            ServicoRequestDTO request = criarRequestValido();
            ServicoResponseDTO response = criarResponseValido(UUID_FIXO, Status.INATIVO);

            when(service.cadastrar(any(ServicoRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(UUID_FIXO.toString()))
                    .andExpect(jsonPath("$.nome").value("Corte de Cabelo"))
                    .andExpect(jsonPath("$.descricao").value("Corte masculino tradicional"))
                    .andExpect(jsonPath("$.duracaoMinutos").value(30))
                    .andExpect(jsonPath("$.preco").value(50.00))
                    .andExpect(jsonPath("$.status").value("INATIVO"));

            verify(service, times(1)).cadastrar(any(ServicoRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando JSON inválido")
        void deveRetornar400QuandoJsonInvalido() throws Exception {
            String jsonInvalido = "{ \"nome\": \"\", \"descricao\": \"Corte\", \"duracaoMinutos\": -5, \"preco\": -10 }";

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonInvalido))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray());

            verify(service, never()).cadastrar(any(ServicoRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict quando nome já existe")
        void deveRetornar409QuandoNomeJaExiste() throws Exception {
            ServicoRequestDTO request = criarRequestValido();

            when(service.cadastrar(any(ServicoRequestDTO.class)))
                    .thenThrow(new ConflitoUnicidadeException("Nome de serviço já cadastrado"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.erro").value("Nome de serviço já cadastrado"));
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
    }

    @Nested
    @DisplayName("GET /spacovip/servicos - Listar Serviços")
    class ListarServicos {

        @Test
        @DisplayName("Deve retornar 200 OK com página de serviços")
        void deveRetornar200ComPaginaDeServicos() throws Exception {
            ServicoResponseDTO response = criarResponseValido(UUID_FIXO, Status.ATIVO);
            Page<ServicoResponseDTO> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

            when(service.buscarTodosServicos(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "nome,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(UUID_FIXO.toString()))
                    .andExpect(jsonPath("$.content[0].nome").value("Corte de Cabelo"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(10));

            verify(service, times(1)).buscarTodosServicos(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar 200 OK com página vazia quando não há serviços")
        void deveRetornar200ComPaginaVazia() throws Exception {
            Page<ServicoResponseDTO> pageVazia = Page.empty();
            when(service.buscarTodosServicos(any(Pageable.class))).thenReturn(pageVazia);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Deve aplicar paginação e ordenação personalizadas")
        void deveAplicarPaginacaoEOrdenacaoPersonalizadas() throws Exception {
            Page<ServicoResponseDTO> page = Page.empty();
            when(service.buscarTodosServicos(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .param("page", "1")
                            .param("size", "5")
                            .param("sort", "id,desc"))
                    .andExpect(status().isOk());

            verify(service, times(1)).buscarTodosServicos(argThat(p ->
                    p.getPageNumber() == 1 &&
                    p.getPageSize() == 5 &&
                    p.getSort().toString().contains("id: DESC")
            ));
        }
    }

    @Nested
    @DisplayName("GET /spacovip/servicos/{id} - Buscar Serviço por ID")
    class BuscarServicoPorId {

        @Test
        @DisplayName("Deve retornar 200 OK com serviço encontrado")
        void deveRetornar200ComServicoEncontrado() throws Exception {
            ServicoResponseDTO response = criarResponseValido(UUID_FIXO, Status.ATIVO);
            when(service.buscarPorId(UUID_FIXO)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/" + UUID_FIXO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(UUID_FIXO.toString()))
                    .andExpect(jsonPath("$.nome").value("Corte de Cabelo"))
                    .andExpect(jsonPath("$.status").value("ATIVO"));

            verify(service, times(1)).buscarPorId(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando serviço não existe")
        void deveRetornar404QuandoServicoNaoExiste() throws Exception {
            UUID idInexistente = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
            when(service.buscarPorId(idInexistente))
                    .thenThrow(new ResourceNotFoundException("Serviço não encontrado com o ID: " + idInexistente));

            mockMvc.perform(get(BASE_URL + "/" + idInexistente))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.erro").value("Serviço não encontrado com o ID: " + idInexistente));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando UUID malformado no path")
        void deveRetornar400QuandoUuidMalformado() throws Exception {
            mockMvc.perform(get(BASE_URL + "/uuid-invalido"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.erro").exists());
        }
    }

    @Nested
    @DisplayName("PUT /spacovip/servicos/{id} - Atualizar Serviço")
    class AtualizarServico {

        @Test
        @DisplayName("Deve retornar 200 OK com serviço atualizado")
        void deveRetornar200ComServicoAtualizado() throws Exception {
            ServicoRequestDTO request = new ServicoRequestDTO(
                    "Corte e Barba",
                    "Corte masculino + barba",
                    45L,
                    new BigDecimal("80.00")
            );
            ServicoResponseDTO response = new ServicoResponseDTO(
                    UUID_FIXO, "Corte e Barba", "Corte masculino + barba", 45L,
                    new BigDecimal("80.00"), Status.ATIVO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            when(service.atualizar(eq(UUID_FIXO), any(ServicoRequestDTO.class))).thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/" + UUID_FIXO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(UUID_FIXO.toString()))
                    .andExpect(jsonPath("$.nome").value("Corte e Barba"))
                    .andExpect(jsonPath("$.duracaoMinutos").value(45))
                    .andExpect(jsonPath("$.preco").value(80.00));

            verify(service, times(1)).atualizar(eq(UUID_FIXO), any(ServicoRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando ID não existe")
        void deveRetornar404QuandoIdNaoExisteAoAtualizar() throws Exception {
            ServicoRequestDTO request = criarRequestValido();
            UUID idInexistente = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

            when(service.atualizar(eq(idInexistente), any(ServicoRequestDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Serviço não encontrado com o ID: " + idInexistente));

            mockMvc.perform(put(BASE_URL + "/" + idInexistente)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando JSON inválido")
        void deveRetornar400QuandoJsonInvalidoAoAtualizar() throws Exception {
            String jsonInvalido = "{ \"nome\": \"\", \"descricao\": \"Corte\", \"duracaoMinutos\": -5, \"preco\": -10 }";

            mockMvc.perform(put(BASE_URL + "/" + UUID_FIXO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonInvalido))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict quando nome de outro serviço")
        void deveRetornar409QuandoNomeDeOutroServico() throws Exception {
            ServicoRequestDTO request = criarRequestValido();

            when(service.atualizar(eq(UUID_FIXO), any(ServicoRequestDTO.class)))
                    .thenThrow(new ConflitoUnicidadeException("Este nome de serviço já está em uso."));

            mockMvc.perform(put(BASE_URL + "/" + UUID_FIXO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }
    }

    @Nested
    @DisplayName("DELETE /spacovip/servicos/{id} - Excluir Serviço")
    class ExcluirServico {

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
            UUID idInexistente = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
            doThrow(new ResourceNotFoundException("Serviço não encontrado para exclusão com o ID: " + idInexistente))
                    .when(service).excluir(idInexistente);

            mockMvc.perform(delete(BASE_URL + "/" + idInexistente))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando UUID malformado no path")
        void deveRetornar400QuandoUuidMalformadoAoExcluir() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/uuid-invalido"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.erro").exists());
        }
    }

    @Nested
    @DisplayName("Casos de Borda e Validações Extras")
    class CasosDeBorda {

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
        @DisplayName("Deve aceitar preço com decimais no JSON")
        void deveAceitarPrecoComDecimais() throws Exception {
            String jsonComPrecoDecimal = """
                    {
                        "nome": "Hidratação",
                        "descricao": "Hidratação profunda",
                        "duracaoMinutos": 60,
                        "preco": "120.50"
                    }
                    """;

            ServicoResponseDTO response = new ServicoResponseDTO(
                    UUID_FIXO, "Hidratação", "Hidratação profunda", 60L,
                    new BigDecimal("120.50"), Status.INATIVO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );
            when(service.cadastrar(any(ServicoRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonComPrecoDecimal))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nome").value("Hidratação"))
                    .andExpect(jsonPath("$.preco").value(120.50));
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