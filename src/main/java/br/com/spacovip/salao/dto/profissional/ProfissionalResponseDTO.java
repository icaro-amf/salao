package br.com.spacovip.salao.dto.profissional;

import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProfissionalResponseDTO(
        UUID id,
        String nome,
        String descricao,
        String email,
        String telefone,
        LocalDate dataNascimento,
        Status status,
        Sexo sexo,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao,
        List<UUID> servicosIds
) {
    public ProfissionalResponseDTO(br.com.spacovip.salao.domain.profissional.Profissional profissional) {
        this(
                profissional.getId(),
                profissional.getNome(),
                profissional.getDescricao(),
                profissional.getEmail(),
                profissional.getTelefone(),
                profissional.getDataNascimento(),
                profissional.getStatus(),
                profissional.getSexo(),
                profissional.getDataCadastro(),
                profissional.getDataAtualizacao(),
                profissional.getServicos() != null
                        ? profissional.getServicos().stream().map(br.com.spacovip.salao.domain.servico.Servico::getId).toList()
                        : List.of()
        );
    }
}
