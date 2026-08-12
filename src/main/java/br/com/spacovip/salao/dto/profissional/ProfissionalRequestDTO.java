package br.com.spacovip.salao.dto.profissional;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ProfissionalRequestDTO(
        @NotBlank(message = "O preenchimento do nome é obrigatório")
        String nome,
        String descricao,

        @NotBlank(message = "O preenchimento do e-mail é obrigatório para validação")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "O preenchimento do telefone é obrigatório")
        @Pattern(regexp = "^\\d{10,11}$", message = "O telefone deve conter apenas números (DDD + número) Ex: 11912346789")
        String telefone,

        @Past(message = "A data de nascimento informada não condiz com a realidade")
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataNascimento
) {
}
