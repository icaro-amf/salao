package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.cliente.Cliente;
import br.com.spacovip.salao.dto.cliente.ClienteRequestDTO;
import br.com.spacovip.salao.dto.cliente.ClienteResponseDTO;
import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.ConflitoUnicidadeException;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.mapper.ClienteMapper;
import br.com.spacovip.salao.repository.ClienteRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @Mock
    private ClienteMapper mapper;

    @InjectMocks
    private ClienteService service;

    private static final LocalDate DATA_NASCIMENTO_FIXA = LocalDate.of(1990, 5, 10);
    private static final LocalDateTime DATA_CADASTRO_FIXA = LocalDateTime.of(2024, 1, 15, 10, 30);
    private static final LocalDateTime DATA_ATUALIZACAO_FIXA = LocalDateTime.of(2024, 1, 20, 14, 45);
    private static final UUID UUID_FIXO = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Nested
    @DisplayName("Cadastrar Cliente")
    class CadastrarCliente {

        @Test
        @DisplayName("Deve cadastrar novo cliente com dados válidos")
        void deveCadastrarNovoClienteComDadosValidos() {
            ClienteRequestDTO request = new ClienteRequestDTO(
                    "Maria Silva",
                    "maria@email.com",
                    "11987654321",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.FEMININO
            );

            Cliente cliente = new Cliente();
            cliente.setId(UUID_FIXO);
            cliente.setNome(request.nome());
            cliente.setEmail(request.email());

            ClienteResponseDTO responseDTO = new ClienteResponseDTO(
                    UUID_FIXO,
                    request.nome(),
                    request.email(),
                    request.telefone(),
                    request.dataNascimento(),
                    Status.ATIVO,
                    request.sexo(),
                    DATA_CADASTRO_FIXA,
                    DATA_ATUALIZACAO_FIXA
            );

            when(mapper.toEntity(request)).thenReturn(cliente);
            when(repository.save(any(Cliente.class))).thenReturn(cliente);
            when(mapper.toResponse(cliente)).thenReturn(responseDTO);

            ClienteResponseDTO resultado = service.cadastrar(request);

            assertNotNull(resultado);
            assertEquals(UUID_FIXO, resultado.id());
            assertEquals("Maria Silva", resultado.nome());

            verify(repository, times(1)).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoUnicidadeException quando email já existe")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            ClienteRequestDTO request = new ClienteRequestDTO(
                    "Maria Silva",
                    "maria@email.com",
                    "11987654321",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.FEMININO
            );

            when(repository.existsByEmail(request.email())).thenReturn(true);

            assertThrows(ConflitoUnicidadeException.class, () -> {
                service.cadastrar(request);
            });

            verify(repository, never()).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoUnicidadeException quando telefone já existe")
        void deveLancarExcecaoQuandoTelefoneJaExiste() {
            ClienteRequestDTO request = new ClienteRequestDTO(
                    "Maria Silva",
                    "maria@email.com",
                    "11987654321",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.FEMININO
            );

            when(repository.existsByEmail(request.email())).thenReturn(false);
            when(repository.existsByTelefone(request.telefone())).thenReturn(true);

            assertThrows(ConflitoUnicidadeException.class, () -> {
                service.cadastrar(request);
            });

            verify(repository, never()).save(any(Cliente.class));
        }
    }

    @Nested
    @DisplayName("Buscar Cliente por ID")
    class BuscarClientePorId {

        @Test
        @DisplayName("Deve retornar cliente quando ID existe")
        void deveRetornarClienteQuandoIdExiste() {
            Cliente cliente = new Cliente();
            cliente.setId(UUID_FIXO);
            cliente.setNome("Maria Silva");
            cliente.setEmail("maria@email.com");
            cliente.setTelefone("11987654321");
            cliente.setDataNascimento(DATA_NASCIMENTO_FIXA);
            cliente.setStatus(Status.ATIVO);
            cliente.setSexo(Sexo.FEMININO);
            cliente.setDataCadastro(DATA_CADASTRO_FIXA);
            cliente.setDataAtualizacao(DATA_ATUALIZACAO_FIXA);

            ClienteResponseDTO responseDTO = new ClienteResponseDTO(
                    UUID_FIXO, "Maria Silva", "maria@email.com", "11987654321", DATA_NASCIMENTO_FIXA, Status.ATIVO, Sexo.FEMININO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA
            );

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(cliente));
            when(mapper.toResponse(cliente)).thenReturn(responseDTO);

            ClienteResponseDTO resultado = service.buscarPorId(UUID_FIXO);

            assertNotNull(resultado);
            assertEquals(UUID_FIXO, resultado.id());
            verify(repository, times(1)).findById(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExiste() {
            UUID idInexistente = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            when(repository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                service.buscarPorId(idInexistente);
            });

            verify(repository, times(1)).findById(idInexistente);
        }
    }

    @Nested
    @DisplayName("Listar Todos Clientes")
    class ListarTodosClientes {

        @Test
        @DisplayName("Deve retornar página mapeada com clientes")
        void deveRetornarPaginaMapeadaComClientes() {
            Cliente cliente = new Cliente();
            cliente.setId(UUID_FIXO);
            cliente.setNome("Maria Silva");
            cliente.setEmail("maria@email.com");
            cliente.setTelefone("11987654321");
            cliente.setDataNascimento(DATA_NASCIMENTO_FIXA);
            cliente.setStatus(Status.ATIVO);
            cliente.setSexo(Sexo.FEMININO);
            cliente.setDataCadastro(DATA_CADASTRO_FIXA);
            cliente.setDataAtualizacao(DATA_ATUALIZACAO_FIXA);

            ClienteResponseDTO responseDTO = new ClienteResponseDTO(
                    UUID_FIXO, "Maria Silva", "maria@email.com", "11987654321", DATA_NASCIMENTO_FIXA, Status.ATIVO, Sexo.FEMININO, DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA
            );

            Page<Cliente> page = new PageImpl<>(List.of(cliente));
            Page<ClienteResponseDTO> pageEsperada = new PageImpl<>(List.of(responseDTO));

            when(repository.findAll(any(Pageable.class))).thenReturn(page);
            when(mapper.toResponse(cliente)).thenReturn(responseDTO);

            Page<ClienteResponseDTO> resultado = service.buscarTodosClientes(Pageable.unpaged());

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals("Maria Silva", resultado.getContent().get(0).nome());
            verify(repository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há clientes")
        void deveRetornarPaginaVaziaQuandoNaoHaClientes() {
            Page<Cliente> pageVazia = Page.empty();
            when(repository.findAll(any(Pageable.class))).thenReturn(pageVazia);

            Page<ClienteResponseDTO> resultado = service.buscarTodosClientes(Pageable.unpaged());

            assertNotNull(resultado);
            assertTrue(resultado.getContent().isEmpty());
            assertEquals(0, resultado.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Atualizar Cliente")
    class AtualizarCliente {

        @Test
        @DisplayName("Deve atualizar cliente com sucesso")
        void deveAtualizarClienteComSucesso() {
            ClienteRequestDTO request = new ClienteRequestDTO(
                    "Maria Santos",
                    "maria.novo@email.com",
                    "11999999999",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.FEMININO
            );

            Cliente clienteExistente = new Cliente();
            clienteExistente.setId(UUID_FIXO);
            clienteExistente.setNome("Maria Silva");
            clienteExistente.setEmail("maria@email.com");
            clienteExistente.setTelefone("11987654321");
            clienteExistente.setDataNascimento(DATA_NASCIMENTO_FIXA);
            clienteExistente.setSexo(Sexo.FEMININO);
            clienteExistente.setStatus(Status.ATIVO);

            Cliente clienteAtualizado = new Cliente();
            clienteAtualizado.setId(UUID_FIXO);
            clienteAtualizado.setNome(request.nome());
            clienteAtualizado.setEmail(request.email());
            clienteAtualizado.setTelefone(request.telefone());
            clienteAtualizado.setDataNascimento(request.dataNascimento());
            clienteAtualizado.setSexo(request.sexo());
            clienteAtualizado.setStatus(Status.ATIVO);
            clienteAtualizado.setDataCadastro(DATA_CADASTRO_FIXA);
            clienteAtualizado.setDataAtualizacao(DATA_ATUALIZACAO_FIXA);

            ClienteResponseDTO responseDTO = new ClienteResponseDTO(
                    UUID_FIXO, request.nome(), request.email(), request.telefone(), request.dataNascimento(), Status.ATIVO, request.sexo(), DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA
            );

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(clienteExistente));
            when(repository.existsByEmail(request.email())).thenReturn(false);
            when(repository.existsByTelefone(request.telefone())).thenReturn(false);
            when(repository.save(any(Cliente.class))).thenReturn(clienteAtualizado);
            when(mapper.toResponse(clienteAtualizado)).thenReturn(responseDTO);

            ClienteResponseDTO resultado = service.atualizar(UUID_FIXO, request);

            assertNotNull(resultado);
            assertEquals("Maria Santos", resultado.nome());
            assertEquals(UUID_FIXO, resultado.id());
            verify(repository, times(1)).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExisteAoAtualizar() {
            ClienteRequestDTO request = new ClienteRequestDTO(
                    "Maria Santos",
                    "maria.novo@email.com",
                    "11999999999",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.FEMININO
            );

            UUID idInexistente = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            when(repository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                service.atualizar(idInexistente, request);
            });

            verify(repository, never()).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoUnicidadeException quando email de outro cliente")
        void deveLancarExcecaoQuandoEmailDeOutroCliente() {
            ClienteRequestDTO request = new ClienteRequestDTO(
                    "Maria Santos",
                    "outro@email.com",
                    "11999999999",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.FEMININO
            );

            Cliente clienteExistente = new Cliente();
            clienteExistente.setId(UUID_FIXO);
            clienteExistente.setEmail("maria@email.com");
            clienteExistente.setTelefone("11987654321");

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(clienteExistente));
            when(repository.existsByEmail(request.email())).thenReturn(true);

            assertThrows(ConflitoUnicidadeException.class, () -> {
                service.atualizar(UUID_FIXO, request);
            });

            verify(repository, never()).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoUnicidadeException quando telefone de outro cliente")
        void deveLancarExcecaoQuandoTelefoneDeOutroCliente() {
            ClienteRequestDTO request = new ClienteRequestDTO(
                    "Maria Santos",
                    "maria.novo@email.com",
                    "11888888888",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.FEMININO
            );

            Cliente clienteExistente = new Cliente();
            clienteExistente.setId(UUID_FIXO);
            clienteExistente.setEmail("maria@email.com");
            clienteExistente.setTelefone("11987654321");

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(clienteExistente));
            when(repository.existsByEmail(request.email())).thenReturn(false);
            when(repository.existsByTelefone(request.telefone())).thenReturn(true);

            assertThrows(ConflitoUnicidadeException.class, () -> {
                service.atualizar(UUID_FIXO, request);
            });

            verify(repository, never()).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve manter mesmo email e telefone sem validar duplicidade contra si mesmo")
        void deveManterMesmoEmailTelefoneSemValidarContraSiMesmo() {
            ClienteRequestDTO request = new ClienteRequestDTO(
                    "Maria Santos",
                    "maria@email.com",
                    "11987654321",
                    DATA_NASCIMENTO_FIXA,
                    Sexo.FEMININO
            );

            Cliente clienteExistente = new Cliente();
            clienteExistente.setId(UUID_FIXO);
            clienteExistente.setEmail("maria@email.com");
            clienteExistente.setTelefone("11987654321");
            clienteExistente.setNome("Maria Silva");
            clienteExistente.setDataNascimento(DATA_NASCIMENTO_FIXA);
            clienteExistente.setSexo(Sexo.FEMININO);
            clienteExistente.setStatus(Status.ATIVO);
            clienteExistente.setDataCadastro(DATA_CADASTRO_FIXA);
            clienteExistente.setDataAtualizacao(DATA_ATUALIZACAO_FIXA);

            Cliente clienteAtualizado = new Cliente();
            clienteAtualizado.setId(UUID_FIXO);
            clienteAtualizado.setNome(request.nome());
            clienteAtualizado.setEmail(request.email());
            clienteAtualizado.setTelefone(request.telefone());
            clienteAtualizado.setDataNascimento(request.dataNascimento());
            clienteAtualizado.setSexo(request.sexo());
            clienteAtualizado.setStatus(Status.ATIVO);
            clienteAtualizado.setDataCadastro(DATA_CADASTRO_FIXA);
            clienteAtualizado.setDataAtualizacao(DATA_ATUALIZACAO_FIXA);

            ClienteResponseDTO responseDTO = new ClienteResponseDTO(
                    UUID_FIXO, request.nome(), request.email(), request.telefone(), request.dataNascimento(), Status.ATIVO, request.sexo(), DATA_CADASTRO_FIXA, DATA_ATUALIZACAO_FIXA
            );

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(clienteExistente));
            when(repository.save(any(Cliente.class))).thenReturn(clienteAtualizado);
            when(mapper.toResponse(clienteAtualizado)).thenReturn(responseDTO);

            ClienteResponseDTO resultado = service.atualizar(UUID_FIXO, request);

            assertNotNull(resultado);
            assertEquals("Maria Santos", resultado.nome());
            verify(repository, never()).existsByEmail(anyString());
            verify(repository, never()).existsByTelefone(anyString());
        }
    }

    @Nested
    @DisplayName("Desativar Cliente")
    class DesativarCliente {

        @Test
        @DisplayName("Deve desativar cliente com sucesso")
        void deveDesativarClienteComSucesso() {
            Cliente cliente = new Cliente();
            cliente.setId(UUID_FIXO);
            cliente.setStatus(Status.ATIVO);

            when(repository.findById(UUID_FIXO)).thenReturn(Optional.of(cliente));

            service.desativar(UUID_FIXO);

            assertEquals(Status.INATIVO, cliente.getStatus());
            verify(repository, times(1)).save(cliente);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExisteAoDesativar() {
            UUID idInexistente = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            when(repository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                service.desativar(idInexistente);
            });

            verify(repository, never()).save(any(Cliente.class));
        }
    }

    @Nested
    @DisplayName("Excluir Cliente")
    class ExcluirCliente {

        @Test
        @DisplayName("Deve excluir cliente com sucesso")
        void deveExcluirClienteComSucesso() {
            when(repository.existsById(UUID_FIXO)).thenReturn(true);

            service.excluir(UUID_FIXO);

            verify(repository, times(1)).deleteById(UUID_FIXO);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoIdNaoExisteAoExcluir() {
            UUID idInexistente = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            when(repository.existsById(idInexistente)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> {
                service.excluir(idInexistente);
            });

            verify(repository, never()).deleteById(any(UUID.class));
        }
    }
}