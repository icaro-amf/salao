package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.profissional.Profissional;
import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.dto.profissional.ProfissionalRequestDTO;
import br.com.spacovip.salao.dto.profissional.ProfissionalResponseDTO;
import br.com.spacovip.salao.enums.Status;
import br.com.spacovip.salao.exception.ConflitoUnicidadeException;
import br.com.spacovip.salao.exception.ResourceNotFoundException;
import br.com.spacovip.salao.mapper.ProfissionalMapper;
import br.com.spacovip.salao.repository.ProfissionalRepository;
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
public class ProfissionalService {

    private final ProfissionalRepository repository;
    private final ServicoRepository servicoRepository;
    private final ProfissionalMapper mapper;
    private final ServicoService servicoService;

    @Transactional
    public ProfissionalResponseDTO cadastrar(ProfissionalRequestDTO request) {
        log.info("Iniciando cadastro de profissional: {}", request.nome());

        if (repository.existsByEmail(request.email())) {
            throw new ConflitoUnicidadeException("E-mail indisponível para uso");
        }

        if (repository.existsByTelefone(request.telefone())) {
            throw new ConflitoUnicidadeException("Telefone indisponível para uso");
        }

        Profissional profissional = mapper.toEntity(request);
        profissional.setStatus(Status.ATIVO);
        Profissional profissionalSalvo = repository.save(profissional);

        if (request.servicosIds() != null && !request.servicosIds().isEmpty()) {
            servicoService.recalcularStatus(request.servicosIds());
        }

        log.info("Profissional cadastrado com sucesso. ID: {}", profissionalSalvo.getId());
        return mapper.toResponse(profissionalSalvo);
    }

    @Transactional(readOnly = true)
    public ProfissionalResponseDTO buscarPorId(UUID id) {
        log.info("Buscando profissional com ID: {}", id);
        Profissional profissional = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com o ID: " + id));
        return mapper.toResponse(profissional);
    }

    @Transactional(readOnly = true)
    public Page<ProfissionalResponseDTO> buscarTodosProfissionais(Pageable paginacao) {
        log.info("Buscando profissionais com paginação.");
        Page<Profissional> paginaDeProfissionais = repository.findAll(paginacao);
        return paginaDeProfissionais.map(mapper::toResponse);
    }

    @Transactional
    public void desativar(UUID id) {
        log.info("Desativando profissional com ID: {}", id);
        Profissional profissional = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com o ID: " + id));

        List<UUID> servicoIds = profissional.getServicos().stream()
                .map(Servico::getId)
                .toList();

        profissional.setStatus(Status.INATIVO);
        repository.save(profissional);

        if (!servicoIds.isEmpty()) {
            servicoService.recalcularStatus(servicoIds);
        }

        log.info("Profissional desativado com sucesso. ID: {}", profissional.getId());
    }

    @Transactional
    public void excluir(UUID id) {
        log.info("Excluindo profissional ID do banco de dados: {}", id);
        Profissional profissional = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado para exclusão com o ID: " + id));

        List<UUID> servicoIds = profissional.getServicos().stream()
                .map(Servico::getId)
                .toList();

        profissional.getServicos().forEach(servico -> servico.getProfissionais().remove(profissional));
        profissional.getServicos().clear();
        repository.save(profissional);
        repository.deleteById(id);

        if (!servicoIds.isEmpty()) {
            servicoService.recalcularStatus(servicoIds);
        }

        log.warn("Profissional excluído com sucesso.");
    }

    @Transactional
    public ProfissionalResponseDTO atualizar(UUID id, ProfissionalRequestDTO request) {
        log.info("Atualizando profissional com ID: {}", id);
        Profissional profissionalExistente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com o ID: " + id));

        if (!profissionalExistente.getEmail().equals(request.email()) && repository.existsByEmail(request.email())) {
            throw new ConflitoUnicidadeException("Este e-mail não está disponível.");
        }

        if (!profissionalExistente.getTelefone().equals(request.telefone()) && repository.existsByTelefone(request.telefone())) {
            throw new ConflitoUnicidadeException("Este telefone não está disponível.");
        }

        List<UUID> servicosAntigos = profissionalExistente.getServicos().stream()
                .map(Servico::getId)
                .toList();

        mapper.updateEntityFromRequest(profissionalExistente, request);

        Profissional profissionalAtualizado = repository.save(profissionalExistente);

        List<UUID> servicosNovos = profissionalAtualizado.getServicos().stream()
                .map(Servico::getId)
                .toList();

        List<UUID> todosServicosAfetados = new java.util.ArrayList<>(servicosAntigos);
        todosServicosAfetados.addAll(servicosNovos);
        todosServicosAfetados = todosServicosAfetados.stream().distinct().toList();

        if (!todosServicosAfetados.isEmpty()) {
            servicoService.recalcularStatus(todosServicosAfetados);
        }

        log.info("Profissional atualizado com sucesso.");

        return mapper.toResponse(profissionalAtualizado);
    }
}