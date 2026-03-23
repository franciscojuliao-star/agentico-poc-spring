package br.ufc.llm.shared.exception;

import br.ufc.llm.auth.exception.ContaInativaException;
import br.ufc.llm.perfil.exception.SenhaAtualInvalidaException;
import br.ufc.llm.perfil.exception.TipoArquivoInvalidoException;
import br.ufc.llm.auth.exception.CredenciaisInvalidasException;
import br.ufc.llm.shared.dto.ApiResponse;
import br.ufc.llm.curso.exception.CursoNaoEncontradoException;
import br.ufc.llm.usuario.exception.CpfJaCadastradoException;
import br.ufc.llm.usuario.exception.EmailJaCadastradoException;
import br.ufc.llm.usuario.exception.UsuarioNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(mensagem, 400));
    }

    @ExceptionHandler(SenhaAtualInvalidaException.class)
    public ResponseEntity<ApiResponse<Void>> handleSenhaAtualInvalida(SenhaAtualInvalidaException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), 400));
    }

    @ExceptionHandler(TipoArquivoInvalidoException.class)
    public ResponseEntity<ApiResponse<Void>> handleTipoArquivoInvalido(TipoArquivoInvalidoException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), 400));
    }

    @ExceptionHandler(CursoNaoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleCursoNaoEncontrado(CursoNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailDuplicado(EmailJaCadastradoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), 409));
    }

    @ExceptionHandler(CpfJaCadastradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleCpfDuplicado(CpfJaCadastradoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), 409));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiResponse<Void>> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), 401));
    }

    @ExceptionHandler(ContaInativaException.class)
    public ResponseEntity<ApiResponse<Void>> handleContaInativa(ContaInativaException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), 403));
    }
}
