package br.com.spacovip.salao.mapper;

import br.com.spacovip.salao.domain.profissional.Profissional;
import br.com.spacovip.salao.dto.profissional.ProfissionalRequestDTO;
import br.com.spacovip.salao.dto.profissional.ProfissionalResponseDTO;

public class ProfissionalMapper {
    public Profissional toEntity(ProfissionalRequestDTO request) {
        if(request == null) return null;

        Profissional profissional = new Profissional();
        profissional.setNome(request.nome());
        profissional.setTelefone(request.telefone());
        profissional.setEmail(request.email());
        profissional.setDataNascimento(request.dataNascimento());
        profissional.setSexo(request.sexo());
        return profissional;
    }
    public ProfissionalResponseDTO toResponse(Profissional entity) {
        if(entity == null) return null;

        return new ProfissionalResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getTelefone(),
                entity.getStatus(),
                entity.getSexo()
        );
    }
}
