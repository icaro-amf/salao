package br.com.spacovip.salao.dto.servico;

import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ServicoResponseDTO(
        UUID id,
        String nome,
        String descricao,
        Long duracaoMinutos,
        BigDecimal preco,
        Status status,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao,
        List<UUID> profissionaisIds
) {
    public ServicoResponseDTO(Servico servico) {
        this(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getDuracaoMinutos(),
                servico.getPreco(),
                servico.getStatus(),
                servico.getDataCadastro(),
                servico.getDataAtualizacao(),
                servico.getProfissionais() != null
                        ? servico.getProfissionais().stream()
                        .filter(p -> p.getStatus() == Status.ATIVO)
                        .map(br.com.spacovip.salao.domain.profissional.Profissional::getId)
                        .toList()
                        : List.of()
        );
    }
}
