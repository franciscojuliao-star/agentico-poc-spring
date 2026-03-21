package br.ufc.llm.auth.controller;

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

    @PostMapping("/cadastro")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cadastrar(@Valid @RequestBody CadastroRequest request) {
        UsuarioResponse response = usuarioService.cadastrar(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Cadastro realizado com sucesso. Aguarde a ativação da sua conta."));
    }
}
