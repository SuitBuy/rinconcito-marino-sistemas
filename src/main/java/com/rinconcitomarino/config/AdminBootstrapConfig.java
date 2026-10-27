package com.rinconcitomarino.config;

import com.rinconcitomarino.model.RolUsuario;
import com.rinconcitomarino.model.UsuarioAdmin;
import com.rinconcitomarino.repository.UsuarioAdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapConfig.class);

    @Bean
    CommandLineRunner seedInitialAdmin(
            UsuarioAdminRepository usuarioAdminRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.bootstrap.enabled:true}") boolean enabled,
            @Value("${app.admin.bootstrap.user:}") String adminUser,
            @Value("${app.admin.bootstrap.password:}") String adminPassword,
            @Value("${app.admin.bootstrap.role:ADMIN}") String adminRole
    ) {
        return args -> {
            if (!enabled) {
                return;
            }

            if (adminUser == null || adminUser.isBlank() || adminPassword == null || adminPassword.isBlank()) {
                log.info("Admin bootstrap skipped: ADMIN_USER and ADMIN_PASSWORD are not configured.");
                return;
            }

            if (usuarioAdminRepository.findByUsuario(adminUser).isPresent()) {
                log.info("Admin bootstrap skipped: user '{}' already exists.", adminUser);
                return;
            }

            UsuarioAdmin usuarioAdmin = new UsuarioAdmin();
            usuarioAdmin.setUsuario(adminUser.trim());
            usuarioAdmin.setPassword(passwordEncoder.encode(adminPassword));
            usuarioAdmin.setRol(RolUsuario.fromDatabaseValue(adminRole));
            usuarioAdminRepository.save(usuarioAdmin);
            log.info("Initial admin user '{}' created with role {}.", usuarioAdmin.getUsuario(), usuarioAdmin.getRol());
        };
    }
}
