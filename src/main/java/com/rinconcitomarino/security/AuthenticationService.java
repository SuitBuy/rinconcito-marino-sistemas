package com.rinconcitomarino.security;

import com.rinconcitomarino.dto.AuthTokenResponse;
import com.rinconcitomarino.dto.LoginRequest;
import com.rinconcitomarino.model.UsuarioAdmin;
import com.rinconcitomarino.repository.UsuarioAdminRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthenticationService {

    private final UsuarioAdminRepository usuarioAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthenticationService(
            UsuarioAdminRepository usuarioAdminRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.usuarioAdminRepository = usuarioAdminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse autenticar(LoginRequest request) {
        UsuarioAdmin usuario = usuarioAdminRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        Instant expiresAt = jwtUtil.calcularExpiracion();
        String token = jwtUtil.generarToken(usuario, expiresAt);
        return new AuthTokenResponse(token, usuario.getUsuario(), usuario.getRol(), expiresAt);
    }

    public long getExpirationMinutes() {
        return jwtUtil.getExpirationMinutes();
    }
}
