package com.rinconcitomarino.config;

import com.rinconcitomarino.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (WebSecurity web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .requestMatchers("/css/**", "/assets/**", "/favicon.ico");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   @Value("${app.security.cookie-name:RM_TOKEN}") String cookieName)
            throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(request -> request.getRequestURI().startsWith("/api/")
                                && (request.getCookies() == null || Arrays.stream(request.getCookies())
                                .noneMatch(cookie -> cookieName.equals(cookie.getName())))))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/", "/reservas", "/login", "/carta", "/css/**", "/assets/**", "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/reservas", "/opiniones", "/login", "/api/auth/login", "/api/reservas", "/api/opiniones"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/logout").hasAnyRole("ADMIN", "EMPLEADO", "LECTOR")
                        .requestMatchers("/admin/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/admin/reservas/nueva", "/admin/reservas/*/editar", "/admin/reservas/guardar").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.POST, "/admin/reservas/*/eliminar", "/admin/opiniones/*/eliminar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/admin/**").hasAnyRole("ADMIN", "EMPLEADO", "LECTOR")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.GET, "/api/reservas/**", "/api/opiniones/**")
                        .hasAnyRole("ADMIN", "EMPLEADO", "LECTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservas/**", "/api/opiniones/**").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/admin")) {
                                response.sendRedirect("/login?expired=true");
                            } else {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                            }
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
