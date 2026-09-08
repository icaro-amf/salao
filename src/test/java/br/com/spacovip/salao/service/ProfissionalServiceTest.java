package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.profissional.Profissional;
import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.dto.profissional.ProfissionalRequestDTO;
import br.com.spacovip.salao.dto.profissional.ProfissionalResponseDTO;
import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.ConflitoUnicidadeException;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.mapper.ProfissionalMapper;
import br.com.spacovip.salao.repository.ProfissionalRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository repository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ProfissionalMapper mapper;

    @Mock
    private ServicoService servicoService;

    @InjectMocks
    private ProfissionalService service;

    private static final LocalDate DATA_NASCIMENTO_FIXA = LocalDate.of(1990, 5, 10);
    private static final LocalDateTime DATA_CADASTRO_FIXA = LocalDateTime.of(2024, 1, 15, 10, 30);
    private static final LocalDateTime DATA_ATUALIZACAO_FIXA = LocalDateTime.of(2024, 1, 20, 14, 45);
    private static final UUID UUID_FIXO = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SERVICO_ID_1 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID SERVICO_ID_2 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Nested
    @DisplayName("Cadastrar Profissional")
    class CadastrarProfissional {

        @Test
        @DisplayName("Deve cadastrar novo profissional com dados válidos")
        void deveCadastrarNovoProfissionalComDadosValidos() {
            ProfissionalRequestDTO request = new ProfissionalRequestDTO(
                    "João Silva",
                    "Cabeleireiro especialista",
                    "joao@email.com",
                    "11987654321",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.MASCULINO,
                    List.of()
            );

            Profissional profissional = new Profissional();
            profissional.setId(UUID_FIXO);
            profissional.setNome(request.nome());
            profissional.setEmail(request.email());

            ProfissionalResponseDTO responseDTO = new ProfissionalResponseDTO(
                    UUID_FIXO, request.nome(), request.descricao(), request.email(), request.telefone(),
                    request.dataNascimento(), Status.ATIVO, request.sexo(), DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            when(mapper.toEntity(request)).thenReturn(profissional);
            when(repository.save(any(Profissional.class))).thenReturn(profissional);
            when(mapper.toResponse(profissional)).thenReturn(responseDTO);

            ProfissionalResponseDTO resultado = service.cadastrar(request);

            assertNotNull(resultado);
            assertEquals(UUID_FIXO, resultado.id());
            assertEquals("João Silva", resultado.nome());
            assertEquals(Status.ATIVO, resultado.status());

            verify(repository, times(1)).save(any(Profissional.class));
        }

        @Test
        @DisplayName("Deve cadastrar profissional com serviços vinculados")
        void deveCadastrarProfissionalComServicosVinculados() {
            ProfissionalRequestDTO request = new ProfissionalRequestDTO(
                    "João Silva",
                    "Cabeleireiro especialista",
                    "joao@email.com",
                    "11987654321",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.MASCULINO,
                    List.of(SERVICO_ID_1, SERVICO_ID_2)
            );

            Profissional profissional = new Profissional();
            profissional.setId(UUID_FIXO);
            profissional.setNome(request.nome());
            profissional.setEmail(request.email());

            ProfissionalResponseDTO responseDTO = new ProfissionalResponseDTO(
                    UUID_FIXO, request.nome(), request.descricao(), request.email(), request.telefone(),
                    request.dataNascimento(), Status.ATIVO, request.sexo(), DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of(SERVICO_ID_1, SERVICO_ID_2)
            );

            when(mapper.toEntity(request)).thenReturn(profissional);
            when(repository.save(any(Profissional.class))).thenReturn(profissional);
            when(mapper.toResponse(profissional)).thenReturn(responseDTO);

            ProfissionalResponseDTO resultado = service.cadastrar(request);

            assertNotNull(resultado);
            assertEquals(2, resultado.servicosIds().size());
            verify(repository, times(1)).save(any(Profissional.class));
            verify(servicoService, times(1)).recalcularStatus(eq(List.of(SERVICO_ID_1, SERVICO_ID_2)));
        }

        @Test
        @DisplayName("Deve lançar ConflitoUnicidadeException quando email já existe")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            ProfissionalRequestDTO request = new ProfissionalRequestDTO(
                    "João Silva",
                    "Cabeleireiro",
                    "joao@email.com",
                    "11987654321",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.MASCULINO,
                    List.of()
            );

            when(repository.existsByEmail(request.email())).thenReturn(true);

            assertThrows(ConflitoUnicidadeException.class, () -> {
                service.cadastrar(request);
            });

            verify(repository, never()).save(any(Profissional.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoUnicidadeException quando telefone já existe")
        void deveLancarExcecaoQuandoTelefoneJaExiste() {
            ProfissionalRequestDTO request = new ProfissionalRequestDTO(
                    "João Silva",
                    "Cabeleireiro",
                    "joao@email.com",
                    "11987654321",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.MASCULINO,
                    List.of()
            );

            when(repository.existsByEmail(request.email())).thenReturn(false);
            when(repository.existsByTelefone(request.telefone())).thenReturn(true);

            assertThrows(ConflitoUnicidadeException.class, () -> {
                service.cadastrar(request);
            });

            verify(repository, never()).save(any(Profissional.class));
        }
    }

    @Nested
    @DisplayName("Buscar Profissional por ID")
    class BuscarProfissionalPorId {

        @Test
        @DisplayName("Deve retornar profissional quando ID existe")
        void deveRetornarProfissionalQuandoIdExiste() {
            Profissional profissional = new Profissional();
            profissional.setId(UUID_FIXO);
            profissional.setNome("João Silva");
            profissional.setEmail("joao@email.com");
            profissional.setTelefone("11987654321");
            profissional.setStatus(Status.ATIVO);
            profissional.setSexo(Sexo.MASCULINO);

            ProfissionalResponseDTO responseDTO = new ProfissionalResponseDTO(
                    UUID_FIXO, "João Silva", "Cabeleireiro", "joao@email.com", "11987654321",
                    DATA_NASCIMENTO_FIXA, Status.ATIVO, Sexo.MASCULINO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(profissional));
            when(mapper.toResponse(profissional)).thenReturn(responseDTO);

            ProfissionalResponseDTO resultado = service.buscarPorId(UUID_FIXO);

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
    @DisplayName("Listar Todos Profissionais")
    class ListarTodosProfissionais {

        @Test
        @DisplayName("Deve retornar página mapeada com profissionais")
        void deveRetornarPaginaMapeadaComProfissionais() {
            Profissional profissional = new Profissional();
            profissional.setId(UUID_FIXO);
            profissional.setNome("João Silva");
            profissional.setEmail("joao@email.com");
            profissional.setTelefone("11987654321");
            profissional.setStatus(Status.ATIVO);
            profissional.setSexo(Sexo.MASCULINO);

            ProfissionalResponseDTO responseDTO = new ProfissionalResponseDTO(
                    UUID_FIXO, "João Silva", "Cabeleireiro", "joao@email.com", "11987654321",
                    DATA_NASCIMENTO_FIXA, Status.ATIVO, Sexo.MASCULINO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            Page<Profissional> page = new PageImpl<>(List.of(profissional));
            when(repository.findAll(any(Pageable.class))).thenReturn(page);
            when(mapper.toResponse(profissional)).thenReturn(responseDTO);

            Page<ProfissionalResponseDTO> resultado = service.buscarTodosProfissionais(Pageable.unpaged());

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals("João Silva", resultado.getContent().get(0).nome());
            verify(repository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há profissionais")
        void deveRetornarPaginaVaziaQuandoNaoHaProfissionais() {
            Page<Profissional> pageVazia = Page.empty();
            when(repository.findAll(any(Pageable.class))).thenReturn(pageVazia);

            Page<ProfissionalResponseDTO> resultado = service.buscarTodosProfissionais(Pageable.unpaged());

            assertNotNull(resultado);
            assertTrue(resultado.getContent().isEmpty());
            assertEquals(0, resultado.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Atualizar Profissional")
    class AtualizarProfissional {

        @Test
        @DisplayName("Deve atualizar profissional com sucesso")
        void deveAtualizarProfissionalComSucesso() {
            ProfissionalRequestDTO request = new ProfissionalRequestDTO(
                    "João Santos",
                    "Barbeiro especialista",
                    "joao.novo@email.com",
                    "11999999999",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.MASCULINO,
                    List.of()
            );

            Profissional profissionalExistente = new Profissional();
            profissionalExistente.setId(UUID_FIXO);
            profissionalExistente.setNome("João Silva");
            profissionalExistente.setEmail("joao@email.com");
            profissionalExistente.setTelefone("11987654321");
            profissionalExistente.setDataNascimento(DATA_NASCIMENTO_FIXA);
            profissionalExistente.setSexo(Sexo.MASCULINO);
            profissionalExistente.setStatus(Status.ATIVO);

            Profissional profissionalAtualizado = new Profissional();
            profissionalAtualizado.setId(UUID_FIXO);
            profissionalAtualizado.setNome(request.nome());
            profissionalAtualizado.setEmail(request.email());
            profissionalAtualizado.setTelefone(request.telefone());
            profissionalAtualizado.setDataNascimento(request.dataNascimento());
            profissionalAtualizado.setSexo(request.sexo());
            profissionalAtualizado.setStatus(Status.ATIVO);

            ProfissionalResponseDTO responseDTO = new ProfissionalResponseDTO(
                    UUID_FIXO, request.nome(), request.descricao(), request.email(), request.telefone(),
                    request.dataNascimento(), Status.ATIVO, request.sexo(), DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(profissionalExistente));
            when(repository.existsByEmail(request.email())).thenReturn(false);
            when(repository.existsByTelefone(request.telefone())).thenReturn(false);
            when(mapper.toResponse(any(Profissional.class))).thenReturn(responseDTO);
            when(repository.save(any(Profissional.class))).thenReturn(profissionalAtualizado);

            ProfissionalResponseDTO resultado = service.atualizar(UUID_FIXO, request);

            assertNotNull(resultado);
            assertEquals("João Santos", resultado.nome());
            assertEquals(UUID_FIXO, resultado.id());
            verify(repository, times(1)).save(any(Profissional.class));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExisteAoAtualizar() {
            ProfissionalRequestDTO request = new ProfissionalRequestDTO(
                    "João Santos",
                    "Barbeiro",
                    "joao.novo@email.com",
                    "11999999999",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.MASCULINO,
                    List.of()
            );

            UUID idInexistente = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
            when(repository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                service.atualizar(idInexistente, request);
            });

            verify(repository, never()).save(any(Profissional.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoUnicidadeException quando email de outro profissional")
        void deveLancarExcecaoQuandoEmailDeOutroProfissional() {
            ProfissionalRequestDTO request = new ProfissionalRequestDTO(
                    "João Santos",
                    "Barbeiro",
                    "outro@email.com",
                    "11999999999",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.MASCULINO,
                    List.of()
            );

            Profissional profissionalExistente = new Profissional();
            profissionalExistente.setId(UUID_FIXO);
            profissionalExistente.setEmail("joao@email.com");
            profissionalExistente.setTelefone("11987654321");

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(profissionalExistente));
            when(repository.existsByEmail(request.email())).thenReturn(true);

            assertThrows(ConflitoUnicidadeException.class, () -> {
                service.atualizar(UUID_FIXO, request);
            });

            verify(repository, never()).save(any(Profissional.class));
        }

        @Test
        @DisplayName("Deve manter mesmo email e telefone sem validar duplicidade contra si mesmo")
        void deveManterMesmoEmailTelefoneSemValidarContraSiMesmo() {
            ProfissionalRequestDTO request = new ProfissionalRequestDTO(
                    "João Santos",
                    "Barbeiro",
                    "joao@email.com",
                    "11987654321",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.MASCULINO,
                    List.of()
            );

            Profissional profissionalExistente = new Profissional();
            profissionalExistente.setId(UUID_FIXO);
            profissionalExistente.setEmail("joao@email.com");
            profissionalExistente.setTelefone("11987654321");
            profissionalExistente.setNome("João Silva");
            profissionalExistente.setDataNascimento(DATA_NASCIMENTO_FIXA);
            profissionalExistente.setSexo(Sexo.MASCULINO);
            profissionalExistente.setStatus(Status.ATIVO);

            Profissional profissionalAtualizado = new Profissional();
            profissionalAtualizado.setId(UUID_FIXO);
            profissionalAtualizado.setNome(request.nome());
            profissionalAtualizado.setEmail(request.email());
            profissionalAtualizado.setTelefone(request.telefone());
            profissionalAtualizado.setDataNascimento(request.dataNascimento());
            profissionalAtualizado.setSexo(request.sexo());
            profissionalAtualizado.setStatus(Status.ATIVO);

            ProfissionalResponseDTO responseDTO = new ProfissionalResponseDTO(
                    UUID_FIXO, request.nome(), request.descricao(), request.email(), request.telefone(),
                    request.dataNascimento(), Status.ATIVO, request.sexo(), DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA, List.of()
            );

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(profissionalExistente));
            when(repository.save(any(Profissional.class))).thenReturn(profissionalAtualizado);
            when(mapper.toResponse(profissionalAtualizado)).thenReturn(responseDTO);

            ProfissionalResponseDTO resultado = service.atualizar(UUID_FIXO, request);

            assertNotNull(resultado);
            assertEquals("João Santos", resultado.nome());
            verify(repository, never()).existsByEmail(anyString());
            verify(repository, never()).existsByTelefone(anyString());
        }
    }

    @Nested
    @DisplayName("Desativar Profissional")
    class DesativarProfissional {

        @Test
        @DisplayName("Deve desativar profissional com sucesso")
        void deveDesativarProfissionalComSucesso() {
            Profissional profissional = new Profissional();
            profissional.setId(UUID_FIXO);
            profissional.setStatus(Status.ATIVO);

            Servico servico1 = new Servico();
            servico1.setId(SERVICO_ID_1);
            servico1.getProfissionais().add(profissional);
            profissional.getServicos().add(servico1);

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(profissional));

            service.desativar(UUID_FIXO);

            assertEquals(Status.INATIVO, profissional.getStatus());
            verify(repository, times(1)).save(profissional);
            verify(servicoService, times(1)).recalcularStatus(eq(List.of(SERVICO_ID_1)));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExisteAoDesativar() {
            UUID idInexistente = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
            when(repository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                service.desativar(idInexistente);
            });

            verify(repository, never()).save(any(Profissional.class));
        }
    }

    @Nested
    @DisplayName("Excluir Profissional")
    class ExcluirProfissional {

        @Test
        @DisplayName("Deve excluir profissional com sucesso")
        void deveExcluirProfissionalComSucesso() {
            Profissional profissional = new Profissional();
            profissional.setId(UUID_FIXO);

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(profissional));

            service.excluir(UUID_FIXO);

            verify(repository, times(1)).save(profissional);
            verify(repository, times(1)).deleteById(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve remover vínculos de serviços ao excluir profissional")
        void deveRemoverVinculosServicosAoExcluir() {
            Profissional profissional = new Profissional();
            profissional.setId(UUID_FIXO);

            Servico servico1 = new Servico();
            servico1.setId(SERVICO_ID_1);
            servico1.getProfissionais().add(profissional);

            Servico servico2 = new Servico();
            servico2.setId(SERVICO_ID_2);
            servico2.getProfissionais().add(profissional);

            profissional.getServicos().add(servico1);
            profissional.getServicos().add(servico2);

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(profissional));

            service.excluir(UUID_FIXO);

            assertTrue(servico1.getProfissionais().isEmpty());
            assertTrue(servico2.getProfissionais().isEmpty());
            assertTrue(profissional.getServicos().isEmpty());
            verify(repository, times(1)).save(profissional);
            verify(repository, times(1)).deleteById(UUID_FIXO);
            verify(servicoService, times(1)).recalcularStatus(anyList());
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
}