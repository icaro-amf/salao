package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.cliente.Cliente;
import br.com.spacovip.salao.dto.cliente.ClienteRequestDTO;
import br.com.spacovip.salao.dto.cliente.ClienteResponseDTO;
import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.mapper.ClienteMapper;
import br.com.spacovip.salao.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @Mock
    private ClienteMapper mapper;

    @InjectMocks
    private ClienteService service;

    @Test
    @DisplayName("Cadastrar novo cliente com dados validados")
    void cadastrarNovoCliente() {
        ClienteRequestDTO request = new ClienteRequestDTO(
                "Maria Silva",
                "maria@email.com",
                "11987654321",
                LocalDate.of(1990, 5, 10),
                Sexo.FEMININO
        );

        UUID idGerado = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(idGerado);
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());

        ClienteResponseDTO responseDTO = new ClienteResponseDTO(
                idGerado,
                request.nome(),
                request.telefone(),
                Status.ATIVO,
                request.sexo()
        );

        when(mapper.toEntity(request)).thenReturn(cliente);
        when(repository.save(any(Cliente.class))).thenReturn(cliente);
        when(mapper.toResponse(cliente)).thenReturn(responseDTO);

        ClienteResponseDTO resultado = service.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(idGerado, resultado.id());
        assertEquals("Maria Silva", resultado.nome());

        verify(repository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando buscar por ID um cliente que não existe")
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.buscarPorId(idInexistente);
        });

        verify(repository, times(1)).findById(idInexistente);
    }
}
