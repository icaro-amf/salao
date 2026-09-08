package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.dto.servico.ServicoRequestDTO;
import br.com.spacovip.salao.dto.servico.ServicoResponseDTO;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.ConflitoUnicidadeException;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.mapper.ServicoMapper;
import br.com.spacovip.salao.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository repository;
    private final ServicoMapper mapper;

    @Transactional
    public ServicoResponseDTO cadastrar(ServicoRequestDTO request) {
        log.info("Iniciando cadastro de serviço: {}", request.nome());

        if (repository.existsByNome(request.nome())) {
            throw new ConflitoUnicidadeException("Nome de serviço já cadastrado");
        }

        Servico servico = mapper.toEntity(request);
        servico.setStatus(Status.INATIVO); // Inicial como INATIVO (sem profissionais)
        Servico servicoSalvo = repository.save(servico);

        log.info("Serviço cadastrado com sucesso. ID: {}", servicoSalvo.getId());
        return mapper.toResponse(servicoSalvo);
    }

    @Transactional(readOnly = true)
    public ServicoResponseDTO buscarPorId(UUID id) {
        log.info("Buscando serviço com ID: {}", id);
        Servico servico = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o ID: " + id));
        return mapper.toResponse(servico);
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponseDTO> buscarTodosServicos(Pageable paginacao) {
        log.info("Buscando serviços com paginação.");
        Page<Servico> paginaDeServicos = repository.findAll(paginacao);
        return paginaDeServicos.map(mapper::toResponse);
    }

    @Transactional
    public ServicoResponseDTO atualizar(UUID id, ServicoRequestDTO request) {
        log.info("Atualizando serviço com ID: {}", id);
        Servico servicoExistente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o ID: " + id));

        if (!servicoExistente.getNome().equals(request.nome()) && repository.existsByNome(request.nome())) {
            throw new ConflitoUnicidadeException("Este nome de serviço já está em uso.");
        }

        mapper.updateEntityFromRequest(servicoExistente, request);
        Servico servicoAtualizado = repository.save(servicoExistente);
        log.info("Serviço atualizado com sucesso.");

        return mapper.toResponse(servicoAtualizado);
    }

    @Transactional
    public void excluir(UUID id) {
        log.info("Excluindo serviço ID do banco de dados: {}", id);
        Servico servico = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado para exclusão com o ID: " + id));

        servico.getProfissionais().forEach(p -> p.getServicos().remove(servico));
        servico.getProfissionais().clear();
        repository.save(servico);
        repository.deleteById(id);
        log.warn("Serviço excluído com sucesso.");
    }

    @Transactional
    public void recalcularStatus(List<UUID> servicoIds) {
        if (servicoIds == null || servicoIds.isEmpty()) {
            return;
        }

        log.info("Recalculando status de {} serviços", servicoIds.size());

        List<UUID> servicosComProfissionalAtivo = repository
                .findServicoIdsWithActiveProfissional(servicoIds, Status.ATIVO);

        for (UUID servicoId : servicoIds) {
            Servico servico = repository.getReferenceById(servicoId);
            boolean temAtivo = servicosComProfissionalAtivo.contains(servicoId);
            Status novoStatus = temAtivo ? Status.ATIVO : Status.INATIVO;

            if (!servico.getStatus().equals(novoStatus)) {
                servico.setStatus(novoStatus);
                repository.save(servico);
                log.debug("Serviço {} status alterado para {}", servicoId, novoStatus);
            }
        }
    }
}