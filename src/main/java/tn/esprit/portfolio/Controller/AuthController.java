package tn.esprit.portfolio.Controller;

// AuthController.java

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.portfolio.DTO.AuthRequest;
import tn.esprit.portfolio.DTO.AuthResponse;
import tn.esprit.portfolio.DTO.UserResponse;
import tn.esprit.portfolio.Entity.User;
import tn.esprit.portfolio.Service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.substring(7); // Remove "Bearer "
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User currentUser = authService.getCurrentUser();
        UserResponse response = UserResponse.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .role(currentUser.getRole())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Pour JWT stateless, on ne fait rien côté serveur
        // Le client doit supprimer le token
        return ResponseEntity.ok().build();
    }
}