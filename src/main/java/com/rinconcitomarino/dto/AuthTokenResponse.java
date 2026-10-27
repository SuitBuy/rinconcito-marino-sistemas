package com.rinconcitomarino.dto;

import com.rinconcitomarino.model.RolUsuario;

import java.time.Instant;

public record AuthTokenResponse(String token, String usuario, RolUsuario rol, Instant expiresAt) {
}
