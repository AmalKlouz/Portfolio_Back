package tn.esprit.portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.portfolio.Entity.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepo extends JpaRepository<Project, Long> {

}
