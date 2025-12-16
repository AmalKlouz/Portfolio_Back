package tn.esprit.portfolio.Config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.esprit.portfolio.Entity.Role;
import tn.esprit.portfolio.Entity.User;
import tn.esprit.portfolio.Repository.UserRepo;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        try {
            String adminEmail = "admin@portfolio.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .username("Administrator") // Ceci est le nom d'affichage
                        .email(adminEmail) // Ceci est l'identifiant pour l'authentification
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                userRepository.save(admin);

                log.info("🎉 Default admin user created!");
                log.info("📧 Email (for login): {}", adminEmail);
                log.info("👤 Display name: {}", admin.getUsername()); // Attention: getUsername() retourne maintenant l'email
                log.info("👤 Actual username field: {}", admin.getDisplayName());
                log.info("🔑 Password: admin123");
            } else {
                log.info("✅ Admin user already exists");
            }
        } catch (Exception e) {
            log.error("❌ Failed to create admin user: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}