package br.com.spacovip.salao.dto.cliente;

import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteResponseDTO(
        UUID id,
        String nome,
        String email,
        String telefone,
        LocalDate dataNascimento,
        Status status,
        Sexo sexo,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {
}
