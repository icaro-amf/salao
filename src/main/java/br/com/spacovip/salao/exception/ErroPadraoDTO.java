package br.com.spacovip.salao.exception;

import java.time.LocalDateTime;

public record ErroPadraoDTO(
        LocalDateTime timestamp,
        Integer status,
        String erro,
        String caminho
) {
}
