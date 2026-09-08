package br.com.spacovip.salao.mapper;

import br.com.spacovip.salao.domain.profissional.Profissional;
import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.dto.profissional.ProfissionalRequestDTO;
import br.com.spacovip.salao.dto.profissional.ProfissionalResponseDTO;
import br.com.spacovip.salao.repository.ServicoRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ProfissionalMapper {

    private final ServicoRepository servicoRepository;

    public ProfissionalMapper(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public Profissional toEntity(ProfissionalRequestDTO request) {
        if (request == null) return null;

        Profissional profissional = new Profissional();
        profissional.setNome(request.nome());
        profissional.setDescricao(request.descricao());
        profissional.setEmail(request.email());
        profissional.setTelefone(request.telefone());
        profissional.setDataNascimento(request.dataNascimento());
        profissional.setSexo(request.sexo());

        if (request.servicosIds() != null && !request.servicosIds().isEmpty()) {
            List<Servico> servicos = new ArrayList<>();
            for (UUID servicoId : request.servicosIds()) {
                Servico servico = servicoRepository.getReferenceById(servicoId);
                servicos.add(servico);
                servico.getProfissionais().add(profissional);
            }
            profissional.setServicos(servicos);
        }

        return profissional;
    }

    public void updateEntityFromRequest(Profissional profissional, ProfissionalRequestDTO request) {
        if (request == null || profissional == null) return;

        profissional.setNome(request.nome());
        profissional.setDescricao(request.descricao());
        profissional.setEmail(request.email());
        profissional.setTelefone(request.telefone());
        profissional.setDataNascimento(request.dataNascimento());
        profissional.setSexo(request.sexo());

        if (request.servicosIds() != null) {
            profissional.getServicos().forEach(s -> s.getProfissionais().remove(profissional));
            profissional.getServicos().clear();

            List<Servico> servicos = new ArrayList<>();
            for (UUID servicoId : request.servicosIds()) {
                Servico servico = servicoRepository.getReferenceById(servicoId);
                servicos.add(servico);
                servico.getProfissionais().add(profissional);
            }
            profissional.setServicos(servicos);
        }
    }

    public ProfissionalResponseDTO toResponse(Profissional entity) {
        if (entity == null) return null;
        return new ProfissionalResponseDTO(entity);
    }
}
