package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.cliente.Cliente;
import br.com.spacovip.salao.dto.cliente.ClienteRequestDTO;
import br.com.spacovip.salao.dto.cliente.ClienteResponseDTO;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.BusinessException;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.mapper.ClienteMapper;
import br.com.spacovip.salao.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository repository;
    private final ClienteMapper mapper;

    public ClienteResponseDTO cadastrar(ClienteRequestDTO request) {
        log.info("Iniciando cadastro de cliente: {}", request.nome());

        if (repository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail indisponível para uso");
        }

        if (repository.existsByTelefone(request.telefone())) {
            throw new BusinessException("Telefone indisponível para uso");
        }

        Cliente cliente = mapper.toEntity(request);
        Cliente clienteSalvo = repository.save(cliente);

        log.info("Cliente cadastrado com sucesso. ID: {}", clienteSalvo.getId());
        return mapper.toResponse(clienteSalvo);
    }

    public ClienteResponseDTO buscarPorId(UUID id) {
        log.info("Buscando cliente com ID: {}", id);
        Cliente cliente = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        return mapper.toResponse(cliente);
    }

    public Page<ClienteResponseDTO> buscarTodosClientes(Pageable paginacao) {
        log.info("Buscando clientes com paginação.");
        Page<Cliente> paginaDeClientes = repository.findAll(paginacao);
        return paginaDeClientes.map(mapper::toResponse);
    }

    public void desativar(UUID id) {
        log.info("Desativando cliente com ID: {}", id);
        Cliente cliente = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        cliente.setStatus(Status.INATIVO);
        repository.save(cliente);
        log.info("Cliente desativado com sucesso. ID: {}", cliente.getId());
    }

    public void excluir(UUID id) {
        log.info("Excluindo cliente ID do banco de dados: {}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente não encontrado para exclusão com o ID: " + id);
        }
        repository.deleteById(id);
        log.warn("Cliente excluído com sucesso.");
    }

    public ClienteResponseDTO atualizar(UUID id, ClienteRequestDTO request) {
        log.info("Atualizando cliente com ID: {}", id);
        Cliente clienteExistente = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));

        if (!clienteExistente.getEmail().equals(request.email()) && repository.existsByEmail(request.email())) {
            throw new BusinessException("Este e-mail não está disponivel.");
        }

        if (!clienteExistente.getTelefone().equals(request.telefone()) && repository.existsByTelefone(request.telefone())) {
            throw new BusinessException("Este telefone não está disponivel.");
        }

        clienteExistente.setNome(request.nome());
        clienteExistente.setEmail(request.email());
        clienteExistente.setTelefone(request.telefone());
        clienteExistente.setDataNascimento(request.dataNascimento());
        clienteExistente.setSexo(request.sexo());

        Cliente clienteAtualizado = repository.save(clienteExistente);
        log.info("Cliente atualizado com sucesso.");

        return mapper.toResponse(clienteAtualizado);
    }
}
