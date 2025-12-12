package tn.esprit.portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.portfolio.Entity.CvFile;
import tn.esprit.portfolio.Entity.Project;

public interface CvFileRepo extends JpaRepository<CvFile, Long> {
}
