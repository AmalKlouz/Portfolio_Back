package tn.esprit.portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.portfolio.Entity.ContactMessage;
import tn.esprit.portfolio.Entity.Project;

public interface ContactMessageRepo extends JpaRepository<ContactMessage, Long> {
}
