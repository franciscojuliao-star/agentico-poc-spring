package br.ufc.llm.usuario.dto;

import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.validation.PerfilCadastroValido;
import jakarta.validation.constraints.*;

public record CadastroRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve estar no formato ###.###.###-##")
        String cpf,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String senha,

        @NotNull(message = "Perfil é obrigatório")
        @PerfilCadastroValido
        PerfilUsuario perfil
) {}
