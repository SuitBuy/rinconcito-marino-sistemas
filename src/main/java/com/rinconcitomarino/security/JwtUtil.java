package com.rinconcitomarino.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rinconcitomarino.model.UsuarioAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String ISSUER = "rinconcito-marino";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long expirationMinutes;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes:480}") long expirationMinutes
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
        this.expirationMinutes = expirationMinutes;
    }

    public String generarToken(UsuarioAdmin usuarioAdmin, Instant expiresAt) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuarioAdmin.getUsuario())
                .withClaim("rol", usuarioAdmin.getRol().name())
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(expiresAt))
                .sign(algorithm);
    }

    public DecodedJWT validar(String token) {
        return verifier.verify(token);
    }

    public Instant calcularExpiracion() {
        return Instant.now().plus(Duration.ofMinutes(expirationMinutes));
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }
}
