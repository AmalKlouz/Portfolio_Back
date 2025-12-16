package tn.esprit.portfolio.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.portfolio.DTO.AuthRequest;
import tn.esprit.portfolio.DTO.AuthResponse;
import tn.esprit.portfolio.DTO.UserResponse;
import tn.esprit.portfolio.Entity.User;
import tn.esprit.portfolio.Repository.UserRepo;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), // Utilisez l'email comme identifiant
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Log pour déboguer
        System.out.println("🔑 Authenticating user:");
        System.out.println("  - Email from request: " + request.getEmail());
        System.out.println("  - User email in DB: " + user.getEmail());
        System.out.println("  - User getUsername(): " + user.getUsername());

        String jwtToken = jwtService.generateToken(user); // Passez l'objet User directement
        String refreshToken = jwtService.generateRefreshToken(user);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername()) // Vérifiez ce que ça retourne
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getJwtExpiration() / 1000)
                .user(userResponse)
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);

                UserResponse userResponse = UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build();

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .expiresIn(jwtService.getJwtExpiration() / 1000)
                        .user(userResponse)
                        .build();
            }
        }
        throw new RuntimeException("Refresh token invalide");
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
    }
}