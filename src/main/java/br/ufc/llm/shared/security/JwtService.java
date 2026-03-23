package br.ufc.llm.shared.security;

import br.ufc.llm.usuario.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String CLAIM_TIPO = "tipo";
    private static final String TIPO_ACCESS = "ACCESS";
    private static final String TIPO_REFRESH = "REFRESH";

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public String gerarAccessToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("perfil", usuario.getPerfil().name())
                .claim(CLAIM_TIPO, TIPO_ACCESS)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String gerarRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_TIPO, TIPO_REFRESH)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extrairEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessTokenValido(String token) {
        return isTokenValidoComTipo(token, TIPO_ACCESS);
    }

    public boolean isRefreshTokenValido(String token) {
        return isTokenValidoComTipo(token, TIPO_REFRESH);
    }

    private boolean isTokenValidoComTipo(String token, String tipoEsperado) {
        try {
            Claims claims = parseClaims(token);
            return tipoEsperado.equals(claims.get(CLAIM_TIPO, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
