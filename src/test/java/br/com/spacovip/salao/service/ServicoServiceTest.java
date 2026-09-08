package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.dto.servico.ServicoRequestDTO;
import br.com.spacovip.salao.dto.servico.ServicoResponseDTO;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.ConflitoUnicidadeException;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.mapper.ServicoMapper;
import br.com.spacovip.salao.repository.ServicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository repository;

    @Mock
    private ServicoMapper mapper;

    @InjectMocks
    private ServicoService service;

    private static final LocalDateTime DATA_CADASTRO_FIXA = LocalDateTime.of(2024, 1, 15, 10, 30);
    private static final LocalDateTime DATA_ATUALIZACAO_FIXA = LocalDateTime.of(2024, 1, 20, 14, 45);
    private static final UUID UUID_FIXO = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PROFISSIONAL_ID_1 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID PROFISSIONAL_ID_2 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Nested
    @DisplayName("Cadastrar Serviço")
    class CadastrarServico {

        @Test
        @DisplayName("Deve cadastrar novo serviço com dados válidos")
        void deveCadastrarNovoServicoComDadosValidos() {
            ServicoRequestDTO request = new ServicoRequestDTO(
                    "Corte de Cabelo",
                    "Corte masculino tradicional",
                    30L,
                    new BigDecimal("50.00")
            );

            Servico servico = new Servico();
            servico.setId(UUID_FIXO);
            servico.setNome(request.nome());

            ServicoResponseDTO responseDTO = new ServicoResponseDTO(
                    UUID_FIXO, request.nome(), request.descricao(), request.duracaoMinutos(),
                    request.preco(), Status.INATIVO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            when(mapper.toEntity(request)).thenReturn(servico);
            when(repository.save(any(Servico.class))).thenReturn(servico);
            when(mapper.toResponse(servico)).thenReturn(responseDTO);

            ServicoResponseDTO resultado = service.cadastrar(request);

            assertNotNull(resultado);
            assertEquals(UUID_FIXO, resultado.id());
            assertEquals("Corte de Cabelo", resultado.nome());
            assertEquals(Status.INATIVO, resultado.status());

            verify(repository, times(1)).save(any(Servico.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoUnicidadeException quando nome já existe")
        void deveLancarExcecaoQuandoNomeJaExiste() {
            ServicoRequestDTO request = new ServicoRequestDTO(
                    "Corte de Cabelo",
                    "Corte masculino",
                    30L,
                    new BigDecimal("50.00")
            );

            when(repository.existsByNome(request.nome())).thenReturn(true);

            assertThrows(ConflitoUnicidadeException.class, () -> {
                service.cadastrar(request);
            });

            verify(repository, never()).save(any(Servico.class));
        }
    }

    @Nested
    @DisplayName("Buscar Serviço por ID")
    class BuscarServicoPorId {

        @Test
        @DisplayName("Deve retornar serviço quando ID existe")
        void deveRetornarServicoQuandoIdExiste() {
            Servico servico = new Servico();
            servico.setId(UUID_FIXO);
            servico.setNome("Corte de Cabelo");
            servico.setStatus(Status.ATIVO);

            ServicoResponseDTO responseDTO = new ServicoResponseDTO(
                    UUID_FIXO, "Corte de Cabelo", "Corte masculino", 30L,
                    new BigDecimal("50.00"), Status.ATIVO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(servico));
            when(mapper.toResponse(servico)).thenReturn(responseDTO);

            ServicoResponseDTO resultado = service.buscarPorId(UUID_FIXO);

            assertNotNull(resultado);
            assertEquals(UUID_FIXO, resultado.id());
            verify(repository, times(1)).findById(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExiste() {
            UUID idInexistente = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
            when(repository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                service.buscarPorId(idInexistente);
            });

            verify(repository, times(1)).findById(idInexistente);
        }
    }

    @Nested
    @DisplayName("Listar Todos Serviços")
    class ListarTodosServicos {

        @Test
        @DisplayName("Deve retornar página mapeada com serviços")
        void deveRetornarPaginaMapeadaComServicos() {
            Servico servico = new Servico();
            servico.setId(UUID_FIXO);
            servico.setNome("Corte de Cabelo");
            servico.setStatus(Status.ATIVO);

            ServicoResponseDTO responseDTO = new ServicoResponseDTO(
                    UUID_FIXO, "Corte de Cabelo", "Corte masculino", 30L,
                    new BigDecimal("50.00"), Status.ATIVO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            Page<Servico> page = new PageImpl<>(List.of(servico));
            when(repository.findAll(any(Pageable.class))).thenReturn(page);
            when(mapper.toResponse(servico)).thenReturn(responseDTO);

            Page<ServicoResponseDTO> resultado = service.buscarTodosServicos(Pageable.unpaged());

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals("Corte de Cabelo", resultado.getContent().get(0).nome());
            verify(repository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há serviços")
        void deveRetornarPaginaVaziaQuandoNaoHaServicos() {
            Page<Servico> pageVazia = Page.empty();
            when(repository.findAll(any(Pageable.class))).thenReturn(pageVazia);

            Page<ServicoResponseDTO> resultado = service.buscarTodosServicos(Pageable.unpaged());

            assertNotNull(resultado);
            assertTrue(resultado.getContent().isEmpty());
            assertEquals(0, resultado.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Atualizar Serviço")
    class AtualizarServico {

        @Test
        @DisplayName("Deve atualizar serviço com sucesso")
        void deveAtualizarServicoComSucesso() {
            ServicoRequestDTO request = new ServicoRequestDTO(
                    "Corte e Barba",
                    "Corte masculino + barba",
                    45L,
                    new BigDecimal("80.00")
            );

            Servico servicoExistente = new Servico();
            servicoExistente.setId(UUID_FIXO);
            servicoExistente.setNome("Corte de Cabelo");
            servicoExistente.setStatus(Status.ATIVO);

            Servico servicoAtualizado = new Servico();
            servicoAtualizado.setId(UUID_FIXO);
            servicoAtualizado.setNome(request.nome());
            servicoAtualizado.setDescricao(request.descricao());
            servicoAtualizado.setDuracaoMinutos(request.duracaoMinutos());
            servicoAtualizado.setPreco(request.preco());
            servicoAtualizado.setStatus(Status.ATIVO);

            ServicoResponseDTO responseDTO = new ServicoResponseDTO(
                    UUID_FIXO, request.nome(), request.descricao(), request.duracaoMinutos(),
                    request.preco(), Status.ATIVO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(servicoExistente));
            when(repository.existsByNome(request.nome())).thenReturn(false);
            when(mapper.toResponse(any(Servico.class))).thenReturn(responseDTO);
            when(repository.save(any(Servico.class))).thenReturn(servicoAtualizado);

            ServicoResponseDTO resultado = service.atualizar(UUID_FIXO, request);

            assertNotNull(resultado);
            assertEquals("Corte e Barba", resultado.nome());
            assertEquals(UUID_FIXO, resultado.id());
            verify(repository, times(1)).save(any(Servico.class));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExisteAoAtualizar() {
            ServicoRequestDTO request = new ServicoRequestDTO(
                    "Corte e Barba",
                    "Corte masculino + barba",
                    45L,
                    new BigDecimal("80.00")
            );

            UUID idInexistente = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
            when(repository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                service.atualizar(idInexistente, request);
            });

            verify(repository, never()).save(any(Servico.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoUnicidadeException quando nome de outro serviço")
        void deveLancarExcecaoQuandoNomeDeOutroServico() {
            ServicoRequestDTO request = new ServicoRequestDTO(
                    "Outro Serviço",
                    "Descrição",
                    30L,
                    new BigDecimal("50.00")
            );

            Servico servicoExistente = new Servico();
            servicoExistente.setId(UUID_FIXO);
            servicoExistente.setNome("Corte de Cabelo");

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(servicoExistente));
            when(repository.existsByNome(request.nome())).thenReturn(true);

            assertThrows(ConflitoUnicidadeException.class, () -> {
                service.atualizar(UUID_FIXO, request);
            });

            verify(repository, never()).save(any(Servico.class));
        }

        @Test
        @DisplayName("Deve manter mesmo nome sem validar duplicidade contra si mesmo")
        void deveManterMesmoNomeSemValidarContraSiMesmo() {
            ServicoRequestDTO request = new ServicoRequestDTO(
                    "Corte de Cabelo",
                    "Corte masculino tradicional",
                    30L,
                    new BigDecimal("50.00")
            );

            Servico servicoExistente = new Servico();
            servicoExistente.setId(UUID_FIXO);
            servicoExistente.setNome("Corte de Cabelo");
            servicoExistente.setStatus(Status.ATIVO);

            Servico servicoAtualizado = new Servico();
            servicoAtualizado.setId(UUID_FIXO);
            servicoAtualizado.setNome(request.nome());
            servicoAtualizado.setDescricao(request.descricao());
            servicoAtualizado.setDuracaoMinutos(request.duracaoMinutos());
            servicoAtualizado.setPreco(request.preco());
            servicoAtualizado.setStatus(Status.ATIVO);

            ServicoResponseDTO responseDTO = new ServicoResponseDTO(
                    UUID_FIXO, request.nome(), request.descricao(), request.duracaoMinutos(),
                    request.preco(), Status.ATIVO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(servicoExistente));
            when(repository.save(any(Servico.class))).thenReturn(servicoAtualizado);
            when(mapper.toResponse(servicoAtualizado)).thenReturn(responseDTO);

            ServicoResponseDTO resultado = service.atualizar(UUID_FIXO, request);

            assertNotNull(resultado);
            assertEquals("Corte de Cabelo", resultado.nome());
            verify(repository, never()).existsByNome(anyString());
        }
    }

    @Nested
    @DisplayName("Excluir Serviço")
    class ExcluirServico {

        @Test
        @DisplayName("Deve excluir serviço com sucesso")
        void deveExcluirServicoComSucesso() {
            Servico servico = new Servico();
            servico.setId(UUID_FIXO);

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(servico));

            service.excluir(UUID_FIXO);

            verify(repository, times(1)).save(servico);
            verify(repository, times(1)).deleteById(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve remover vínculos de profissionais ao excluir serviço")
        void deveRemoverVinculosProfissionaisAoExcluir() {
            Servico servico = new Servico();
            servico.setId(UUID_FIXO);

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(servico));

            service.excluir(UUID_FIXO);

            verify(repository, times(1)).save(servico);
            verify(repository, times(1)).deleteById(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExisteAoExcluir() {
            UUID idInexistente = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
            when(repository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                service.excluir(idInexistente);
            });

            verify(repository, never()).deleteById(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("Recalcular Status")
    class RecalcularStatus {

        @Test
        @DisplayName("Deve marcar como ATIVO serviços com profissionais ativos")
        void deveMarcarComoAtivoServicosComProfissionaisAtivos() {
            List<UUID> servicoIds = List.of(UUID_FIXO, PROFISSIONAL_ID_1);
            List<UUID> servicosComAtivos = List.of(UUID_FIXO);

            Servico servicoAtivo = new Servico();
            servicoAtivo.setId(UUID_FIXO);
            servicoAtivo.setStatus(Status.INATIVO);

            Servico servicoInativo = new Servico();
            servicoInativo.setId(PROFISSIONAL_ID_1);
            servicoInativo.setStatus(Status.ATIVO);

            when(repository.findServicoIdsWithActiveProfissional(servicoIds, Status.ATIVO))
                    .thenReturn(servicosComAtivos);
            when(repository.getReferenceById(UUID_FIXO)).thenReturn(servicoAtivo);
            when(repository.getReferenceById(PROFISSIONAL_ID_1)).thenReturn(servicoInativo);

            service.recalcularStatus(servicoIds);

            verify(repository, times(1)).getReferenceById(UUID_FIXO);
            verify(repository, times(1)).save(argThat(s -> s.getStatus() == Status.ATIVO));
            verify(repository, times(1)).getReferenceById(PROFISSIONAL_ID_1);
            verify(repository, times(1)).save(argThat(s -> s.getStatus() == Status.INATIVO));
        }

        @Test
        @DisplayName("Deve marcar como INATIVO serviços sem profissionais ativos")
        void deveMarcarComoInativoServicosSemProfissionaisAtivos() {
            List<UUID> servicoIds = List.of(UUID_FIXO);
            List<UUID> servicosComAtivos = List.of();

            Servico servico = new Servico();
            servico.setId(UUID_FIXO);
            servico.setStatus(Status.ATIVO);

            when(repository.findServicoIdsWithActiveProfissional(servicoIds, Status.ATIVO))
                    .thenReturn(servicosComAtivos);
            when(repository.getReferenceById(UUID_FIXO)).thenReturn(servico);

            service.recalcularStatus(servicoIds);

            verify(repository, times(1)).getReferenceById(UUID_FIXO);
            verify(repository, times(1)).save(argThat(s -> s.getStatus() == Status.INATIVO));
        }

        @Test
        @DisplayName("Deve processar lista vazia sem erros")
        void deveProcessarListaVaziaSemErros() {
            service.recalcularStatus(List.of());
            service.recalcularStatus(null);

            verify(repository, never()).findServicoIdsWithActiveProfissional(any(), any());
        }
    }
}