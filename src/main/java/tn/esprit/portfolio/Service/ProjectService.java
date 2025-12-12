package tn.esprit.portfolio.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.portfolio.Entity.Project;
import tn.esprit.portfolio.Entity.ProjectImage;
import tn.esprit.portfolio.IService.IProjectService;
import tn.esprit.portfolio.Repository.ProjectImageRepo;
import tn.esprit.portfolio.Repository.ProjectRepo;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService implements IProjectService {

    private final ProjectRepo projectRepository;
    private final ProjectImageRepo projectImageRepository;

    @Override
    @Transactional
    public Project createProject(Project project) {
        log.info("🔄 Début createProject");
        log.info("Projet à créer: {}", project);

        // Sauvegarder d'abord le projet
        Project savedProject = projectRepository.save(project);
        log.info("✅ Projet sauvegardé - ID: {}", savedProject.getId());

        // Sauvegarder les images avec la relation correcte
        if (project.getImages() != null && !project.getImages().isEmpty()) {
            log.info("📸 Sauvegarde de {} images", project.getImages().size());

            for (ProjectImage image : project.getImages()) {
                image.setProject(savedProject); // Établir la relation
                log.info("   Image avant sauvegarde - URL: {}, Project: {}",
                        image.getImageUrl(), image.getProject() != null ? image.getProject().getId() : "null");

                ProjectImage savedImage = projectImageRepository.save(image);
                log.info("   ✅ Image sauvegardée - ID: {}, URL: {}, Project ID: {}",
                        savedImage.getId(), savedImage.getImageUrl(), savedImage.getProject().getId());
            }
        } else {
            log.info("⚠️ Aucune image dans le projet");
        }

        log.info("🏁 Fin createProject - Projet ID: {}", savedProject.getId());
        return savedProject;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        log.info("🔄 Récupération de tous les projets");
        List<Project> projects = projectRepository.findAll();

        // Force le chargement des images
        for (Project project : projects) {
            if (project.getImages() != null) {
                project.getImages().size(); // Force le chargement
                log.info("Projet ID: {} - Titre: {} - Images: {}",
                        project.getId(), project.getTitle(), project.getImages().size());
            }
        }

        log.info("✅ {} projets récupérés", projects.size());
        return projects;
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        log.info("🔄 Récupération du projet ID: {}", id);
        Optional<Project> project = projectRepository.findById(id);

        if (project.isPresent()) {
            // Force le chargement des images
            if (project.get().getImages() != null) {
                project.get().getImages().size();
                log.info("✅ Projet trouvé - ID: {} - Titre: {} - Images: {}",
                        id, project.get().getTitle(), project.get().getImages().size());
            }
            return project.get();
        } else {
            log.error("❌ Projet non trouvé - ID: {}", id);
            throw new RuntimeException("Project not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public Project updateProject(Long id, Project updatedProject) {
        log.info("🔄 Mise à jour du projet ID: {}", id);
        Project project = getProjectById(id);

        project.setTitle(updatedProject.getTitle());
        project.setDescription(updatedProject.getDescription());
        project.setTechnologies(updatedProject.getTechnologies());

        Project saved = projectRepository.save(project);
        log.info("✅ Projet mis à jour - ID: {}", id);
        return saved;
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        log.info("🗑️ Suppression du projet ID: {}", id);
        Project project = getProjectById(id);

        // Supprimer d'abord les images associées
        if (project.getImages() != null && !project.getImages().isEmpty()) {
            log.info("Suppression de {} images", project.getImages().size());
            projectImageRepository.deleteAll(project.getImages());
        }

        // Puis supprimer le projet
        projectRepository.delete(project);
        log.info("✅ Projet supprimé - ID: {}", id);
    }
}