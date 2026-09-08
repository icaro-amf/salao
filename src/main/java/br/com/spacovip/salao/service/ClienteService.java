package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.cliente.Cliente;
import br.com.spacovip.salao.dto.cliente.ClienteRequestDTO;
import br.com.spacovip.salao.dto.cliente.ClienteResponseDTO;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.ConflitoUnicidadeException;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.mapper.ClienteMapper;
import br.com.spacovip.salao.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository repository;
    private final ClienteMapper mapper;

    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO request) {
        log.info("Iniciando cadastro de cliente: {}", request.nome());

        if (repository.existsByEmail(request.email())) {
            throw new ConflitoUnicidadeException("E-mail indisponível para uso");
        }

        if (repository.existsByTelefone(request.telefone())) {
            throw new ConflitoUnicidadeException("Telefone indisponível para uso");
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

    @Transactional
    public void desativar(UUID id) {
        log.info("Desativando cliente com ID: {}", id);
        Cliente cliente = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        cliente.setStatus(Status.INATIVO);
        repository.save(cliente);
        log.info("Cliente desativado com sucesso. ID: {}", cliente.getId());
    }

    @Transactional
    public void excluir(UUID id) {
        log.info("Excluindo cliente ID do banco de dados: {}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente não encontrado para exclusão com o ID: " + id);
        }
        repository.deleteById(id);
        log.warn("Cliente excluído com sucesso.");
    }

    @Transactional
    public ClienteResponseDTO atualizar(UUID id, ClienteRequestDTO request) {
        log.info("Atualizando cliente com ID: {}", id);
        Cliente clienteExistente = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));

        if (!clienteExistente.getEmail().equals(request.email()) && repository.existsByEmail(request.email())) {
            throw new ConflitoUnicidadeException("Este e-mail não está disponivel.");
        }

        if (!clienteExistente.getTelefone().equals(request.telefone()) && repository.existsByTelefone(request.telefone())) {
            throw new ConflitoUnicidadeException("Este telefone não está disponivel.");
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

    @Transactional
    public void processarInativacaoAutomatica() {
        log.info("Iniciando rotina de inativação automática de clientes");
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
        List<Cliente> clientesParaInativar = repository.findClientesParaInativacao(Status.ATIVO, cutoff);
        
        for (Cliente cliente : clientesParaInativar) {
            cliente.setStatus(Status.INATIVO);
            repository.save(cliente);
            log.debug("Cliente inativado automaticamente: {}", cliente.getId());
        }
        
        log.info("Rotina de inativação concluída. {} clientes inativados", clientesParaInativar.size());
    }

    @Transactional
    public void excluirClientesInativosAntigos() {
        log.info("Iniciando rotina de exclusão de clientes inativos antigos");
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
        List<Cliente> clientesParaExcluir = repository.findClientesInativosParaExclusao(Status.INATIVO, cutoff);
        
        for (Cliente cliente : clientesParaExcluir) {
            repository.delete(cliente);
            log.debug("Cliente inativo antigo excluído: {}", cliente.getId());
        }
        
        log.info("Rotina de exclusão concluída. {} clientes excluídos", clientesParaExcluir.size());
    }
}
