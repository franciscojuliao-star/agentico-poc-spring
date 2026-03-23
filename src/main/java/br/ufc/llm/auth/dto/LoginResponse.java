package br.ufc.llm.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {}
