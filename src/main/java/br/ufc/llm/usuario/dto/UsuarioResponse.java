package br.ufc.llm.usuario.dto;

import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String cpf,
        String email,
        PerfilUsuario perfil,
        StatusUsuario status,
        LocalDateTime criadoEm
) {}
