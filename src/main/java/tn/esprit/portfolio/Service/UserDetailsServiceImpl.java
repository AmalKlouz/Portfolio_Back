package tn.esprit.portfolio.Service;// UserDetailsServiceImpl.java


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.esprit.portfolio.Entity.User;
import tn.esprit.portfolio.Repository.UserRepo;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepo userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Utilisez User, pas AppUser
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

        // User implémente déjà UserDetails, donc retournez-le directement
        return user;
    }
}