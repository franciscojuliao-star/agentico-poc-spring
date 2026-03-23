package br.ufc.llm.shared.exception;

import br.ufc.llm.auth.exception.ContaInativaException;
import br.ufc.llm.perfil.exception.SenhaAtualInvalidaException;
import br.ufc.llm.perfil.exception.TipoArquivoInvalidoException;
import br.ufc.llm.auth.exception.CredenciaisInvalidasException;
import br.ufc.llm.shared.dto.ApiResponse;
import br.ufc.llm.curso.exception.CursoNaoEncontradoException;
import br.ufc.llm.curso.exception.TransicaoStatusInvalidaException;
import br.ufc.llm.aula.exception.AulaNaoEncontradaException;
import br.ufc.llm.aula.exception.ConteudoGeradoAusenteException;
import br.ufc.llm.aula.exception.ConteudoInsuficienteException;
import br.ufc.llm.prova.exception.AlternativaNaoEncontradaException;
import br.ufc.llm.prova.exception.PerguntaNaoEncontradaException;
import br.ufc.llm.prova.exception.ProvaJaExisteException;
import br.ufc.llm.prova.exception.ProvaNaoEncontradaException;
import br.ufc.llm.modulo.exception.ModuloNaoEncontradoException;
import br.ufc.llm.usuario.exception.CpfJaCadastradoException;
import br.ufc.llm.usuario.exception.EmailJaCadastradoException;
import br.ufc.llm.usuario.exception.UsuarioNaoEncontradoException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String mensagem = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(java.util.stream.Collectors.joining("; "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(mensagem, 400));
    }

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

    @ExceptionHandler(TransicaoStatusInvalidaException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransicaoStatusInvalida(TransicaoStatusInvalidaException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), 422));
    }

    @ExceptionHandler(ProvaJaExisteException.class)
    public ResponseEntity<ApiResponse<Void>> handleProvaJaExiste(ProvaJaExisteException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), 409));
    }

    @ExceptionHandler(ProvaNaoEncontradaException.class)
    public ResponseEntity<ApiResponse<Void>> handleProvaNaoEncontrada(ProvaNaoEncontradaException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(PerguntaNaoEncontradaException.class)
    public ResponseEntity<ApiResponse<Void>> handlePerguntaNaoEncontrada(PerguntaNaoEncontradaException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(AlternativaNaoEncontradaException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlternativaNaoEncontrada(AlternativaNaoEncontradaException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(ConteudoInsuficienteException.class)
    public ResponseEntity<ApiResponse<Void>> handleConteudoInsuficiente(ConteudoInsuficienteException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), 422));
    }

    @ExceptionHandler(ConteudoGeradoAusenteException.class)
    public ResponseEntity<ApiResponse<Void>> handleConteudoGeradoAusente(ConteudoGeradoAusenteException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), 422));
    }

    @ExceptionHandler(AulaNaoEncontradaException.class)
    public ResponseEntity<ApiResponse<Void>> handleAulaNaoEncontrada(AulaNaoEncontradaException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(ModuloNaoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleModuloNaoEncontrado(ModuloNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
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
