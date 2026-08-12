package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.cliente.Cliente;
import br.com.spacovip.salao.dto.cliente.ClienteRequestDTO;
import br.com.spacovip.salao.dto.cliente.ClienteResponseDTO;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.mapper.ClienteMapper;
import br.com.spacovip.salao.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository repository;
    private final ClienteMapper mapper;

    public ClienteResponseDTO cadastrar(ClienteRequestDTO request) {
        log.info("Iniciando cadastro de cliente: {}", request.nome());

        Cliente cliente = mapper.toEntity(request);
        Cliente clienteSalvo = repository.save(cliente);

        log.info("Cliente cadastrado com sucesso. ID: {}", clienteSalvo.getId());
        return mapper.toResponse(clienteSalvo);
    }

    public ClienteResponseDTO buscarPorId(UUID id) {
        log.info("Buscando cliente com ID: {}", id);
        Cliente cliente = repository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID: " + id));
        return mapper.toResponse(cliente);
    }

    public List<ClienteResponseDTO> buscarTodosClientes() {
        log.info("Buscando todos os clientes.");
        List<Cliente> clientes = repository.findAll();
        return clientes.stream().map(mapper::toResponse).toList();
    }

    public void desativarCliente(UUID id) {
        log.info("Desativando cliente com ID: {}", id);
        Cliente cliente = repository.findById(id).orElseThrow(() -> new RuntimeException("Cliente nao encontrado para a desativacao com o ID: " + id));
        cliente.setStatus(Status.INATIVO);
        repository.save(cliente);
        log.info("Cliente desativado com sucesso. ID: {}", cliente.getId());
    }

    public void excluirCliente(UUID id) {
        log.info("Excluindo cliente ID do banco de dados: {}", id);
        if (!repository.existsById(id)) {
            throw new RuntimeException("Cliente não encontrado para exclusão");
        }
        repository.deleteById(id);
        log.warn("Cliente excluído com sucesso.");
    }

    public ClienteResponseDTO atualizarCliente(UUID id, ClienteRequestDTO request) {
        log.info("Atualizando cliente com ID: {}", id);
        Cliente clienteExistente = repository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado para atualização com ID: " + id));

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
