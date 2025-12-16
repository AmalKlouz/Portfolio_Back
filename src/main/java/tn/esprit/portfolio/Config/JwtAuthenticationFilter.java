package tn.esprit.portfolio.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.esprit.portfolio.Service.JwtService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestPath = request.getServletPath();
        log.debug("🔄 Processing request to: {}", requestPath);

        // Skip JWT filter for login and refresh endpoints
        if (requestPath.equals("/api/auth/login") || requestPath.equals("/api/auth/refresh")) {
            log.debug("⏭️ Skipping JWT filter for authentication endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        log.debug("📄 Authorization header: {}", authHeader);

        // Pour /api/auth/me, nous AVONS besoin d'un token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("⚠️ No valid Bearer token found for protected endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final String userEmail = jwtService.extractUsername(jwt);
            log.debug("📧 Extracted email from JWT: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.debug("✅ User details loaded: {}", userDetails.getUsername());

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("🔓 JWT token is valid for user: {}", userEmail);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("✅ Authentication set in SecurityContext for user: {}", userEmail);
                } else {
                    log.warn("❌ JWT token is invalid for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("💥 Error processing JWT token: {}", e.getMessage());
            // Ne pas bloquer la requête, laissez Spring Security gérer
        }

        filterChain.doFilter(request, response);
    }
}