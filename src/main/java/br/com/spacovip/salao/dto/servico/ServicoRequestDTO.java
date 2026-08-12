package br.com.spacovip.salao.dto.servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ServicoRequestDTO(
        @NotBlank(message = "O nome do serviço é obrigatório")
        String nome,

        @NotBlank(message = "A descrição do serviço prestado deve ser informada")
        String descricao,

        @NotNull(message = "A duração é obrigatória")
        @Positive(message = "A duração deve ser maior que zero minutos")
        Long duracaoMinutos,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal preco
) {
}
