package br.com.spacovip.salao.dto.cliente;

import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;

import java.util.UUID;

public record ClienteResponseDTO(
        UUID id,
        String nome,
        String telefone,
        Status status,
        Sexo sexo
) {
}
