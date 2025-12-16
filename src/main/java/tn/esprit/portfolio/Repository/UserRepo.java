package tn.esprit.portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.portfolio.Entity.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
