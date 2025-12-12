package tn.esprit.portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.portfolio.Entity.ProjectImage;

public interface ProjectImageRepo extends JpaRepository<ProjectImage, Long> {
}
