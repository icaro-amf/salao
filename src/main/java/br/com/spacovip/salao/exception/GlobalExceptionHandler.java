package br.com.spacovip.salao.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErroValidacaoDTO>> tratarErroValidacao(MethodArgumentNotValidException ex) {
        List<FieldError> errosDoSpring = ex.getFieldErrors(); //pega todos os erros definidos nas requestDTO

        List<ErroValidacaoDTO> errosMapeados = errosDoSpring.stream()
                .map(erro -> new ErroValidacaoDTO(erro.getField(), erro.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(errosMapeados);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroPadraoDTO> tratarResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErroPadraoDTO> tratarBusinessException(BusinessException ex, HttpServletRequest request) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }
}
