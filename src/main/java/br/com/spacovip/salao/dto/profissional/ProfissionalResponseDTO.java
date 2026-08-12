package br.com.spacovip.salao.dto.profissional;

import br.com.spacovip.salao.domain.profissional.Profissional;
import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;

import java.util.UUID;

public record ProfissionalResponseDTO(
        UUID id,
        String nome,
        String descricao,
        String telefone,
        Status status,
        Sexo sexo
) {
    public ProfissionalResponseDTO(Profissional profissional) {
        this(
                profissional.getId(),
                profissional.getNome(),
                profissional.getDescricao(),
                profissional.getTelefone(),
                profissional.getStatus(),
                profissional.getSexo()
        );
        }
}
