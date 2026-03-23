package br.ufc.llm.perfil.dto;

import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;

import java.time.LocalDateTime;

public record PerfilResponse(
        Long id,
        String nome,
        String cpf,
        String email,
        PerfilUsuario perfil,
        StatusUsuario status,
        String fotoPerfil,
        LocalDateTime criadoEm
) {}
