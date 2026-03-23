package br.ufc.llm.auth.controller;

import br.ufc.llm.auth.dto.LoginRequest;
import br.ufc.llm.auth.dto.LoginResponse;
import br.ufc.llm.auth.dto.RefreshRequest;
import br.ufc.llm.auth.service.AuthService;
import br.ufc.llm.shared.dto.ApiResponse;
import br.ufc.llm.usuario.dto.CadastroRequest;
import br.ufc.llm.usuario.dto.UsuarioResponse;
import br.ufc.llm.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    @PostMapping("/cadastro")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cadastrar(@Valid @RequestBody CadastroRequest request) {
        UsuarioResponse response = usuarioService.cadastrar(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Cadastro realizado com sucesso. Aguarde a ativação da sua conta."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Login realizado com sucesso."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(response, "Token renovado com sucesso."));
    }
}
