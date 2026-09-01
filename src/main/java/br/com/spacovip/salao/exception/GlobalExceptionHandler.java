package br.com.spacovip.salao.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErroValidacaoDTO>> tratarErroValidacao(MethodArgumentNotValidException ex) {
        List<FieldError> errosDoSpring = ex.getFieldErrors();

        List<ErroValidacaoDTO> errosMapeados = errosDoSpring.stream()
                .map(erro -> new ErroValidacaoDTO(erro.getField(), erro.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(errosMapeados);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ErroValidacaoDTO>> tratarConstraintViolation(ConstraintViolationException ex) {
        List<ErroValidacaoDTO> erros = ex.getConstraintViolations().stream()
                .map(v -> new ErroValidacaoDTO(v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroPadraoDTO> tratarJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String mensagem = "JSON inválido ou formato de data incorreto. Use dd/MM/yyyy para datas.";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            mensagem = "Erro de formatação: " + ex.getCause().getMessage();
        }
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroPadraoDTO> tratarTipoIncompativel(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String nomeParam = ex.getName();
        String tipoEsperado = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido";
        String mensagem = String.format("Parâmetro '%s' inválido. Esperado tipo: %s", nomeParam, tipoEsperado);
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroPadraoDTO> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String mensagem = String.format("Método HTTP '%s' não suportado para este endpoint. Métodos permitidos: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(erro);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroPadraoDTO> tratarMediaTypeInvalido(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        String mensagem = String.format("Content-Type '%s' não suportado. Tipos aceitos: %s",
                ex.getContentType(), ex.getSupportedMediaTypes());
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(erro);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErroPadraoDTO> tratarEndpointNaoEncontrado(NoHandlerFoundException ex, HttpServletRequest request) {
        String mensagem = String.format("Endpoint não encontrado: %s %s", ex.getHttpMethod(), ex.getRequestURL());
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(ConflitoUnicidadeException.class)
    public ResponseEntity<ErroPadraoDTO> tratarConflitoUnicidade(ConflitoUnicidadeException ex, HttpServletRequest request) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroPadraoDTO> tratarIntegridadeDados(DataIntegrityViolationException ex, HttpServletRequest request) {
        String mensagem = "Violação de integridade dos dados";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String causeMsg = ex.getCause().getMessage().toLowerCase();
            if (causeMsg.contains("unique") || causeMsg.contains("duplicate") || causeMsg.contains("uk_")) {
                mensagem = "Valor duplicado: já existe um registro com esta informação";
            } else if (causeMsg.contains("foreign key") || causeMsg.contains("fk_")) {
                mensagem = "Não é possível excluir: existem registros vinculados a esta entidade";
            }
        }
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadraoDTO> tratarErroGenerico(Exception ex, HttpServletRequest request) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}