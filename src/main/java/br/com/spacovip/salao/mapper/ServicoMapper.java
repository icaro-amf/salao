package br.com.spacovip.salao.mapper;

import br.com.spacovip.salao.domain.profissional.Profissional;
import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.dto.servico.ServicoRequestDTO;
import br.com.spacovip.salao.dto.servico.ServicoResponseDTO;
import br.com.spacovip.salao.repository.ProfissionalRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ServicoMapper {

    private final ProfissionalRepository profissionalRepository;

    public ServicoMapper(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    public Servico toEntity(ServicoRequestDTO request) {
        if (request == null) return null;

        Servico servico = new Servico();
        servico.setNome(request.nome());
        servico.setDescricao(request.descricao());
        servico.setDuracaoMinutos(request.duracaoMinutos());
        servico.setPreco(request.preco());

        return servico;
    }

    public void updateEntityFromRequest(Servico servico, ServicoRequestDTO request) {
        if (request == null || servico == null) return;

        servico.setNome(request.nome());
        servico.setDescricao(request.descricao());
        servico.setDuracaoMinutos(request.duracaoMinutos());
        servico.setPreco(request.preco());
    }

    public ServicoResponseDTO toResponse(Servico entity) {
        if (entity == null) return null;
        return new ServicoResponseDTO(entity);
    }
}