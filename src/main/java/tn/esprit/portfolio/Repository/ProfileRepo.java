package tn.esprit.portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.portfolio.Entity.Profile;
import tn.esprit.portfolio.Entity.Project;

public interface ProfileRepo extends JpaRepository<Profile, Long> {
}
