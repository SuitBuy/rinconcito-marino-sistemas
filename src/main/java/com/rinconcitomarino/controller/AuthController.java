package com.rinconcitomarino.controller;

import com.rinconcitomarino.dto.AuthTokenResponse;
import com.rinconcitomarino.dto.LoginRequest;
import com.rinconcitomarino.security.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.util.Map;

@Controller
public class AuthController {

    private final AuthenticationService authenticationService;
    private final String cookieName;
    private final boolean cookieSecure;

    public AuthController(
            AuthenticationService authenticationService,
            @Value("${app.security.cookie-name:RM_TOKEN}") String cookieName,
            @Value("${app.security.cookie-secure:false}") boolean cookieSecure
    ) {
        this.authenticationService = authenticationService;
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> loginApi(@Valid @RequestBody LoginRequest request) {
        try {
            AuthTokenResponse token = authenticationService.autenticar(request);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales invalidas"));
        }
    }

    @PostMapping("/login")
    public String loginWeb(
            @Valid LoginRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/login";
        }

        try {
            AuthTokenResponse token = authenticationService.autenticar(request);
            response.addHeader(HttpHeaders.SET_COOKIE, crearCookie(token.token(), httpRequest).toString());
            return "redirect:/admin";
        } catch (BadCredentialsException ex) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, limpiarCookie(request).toString());
        return "redirect:/login";
    }

    private ResponseCookie crearCookie(String token, HttpServletRequest request) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure || request.isSecure())
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(authenticationService.getExpirationMinutes()))
                .build();
    }
    private ResponseCookie limpiarCookie(HttpServletRequest request) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure || request.isSecure())
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}
