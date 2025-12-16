package tn.esprit.portfolio.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.portfolio.DTO.ProjectDTO;
import tn.esprit.portfolio.DTO.ProjectImageDTO;
import tn.esprit.portfolio.Entity.Project;
import tn.esprit.portfolio.Entity.ProjectImage;
import tn.esprit.portfolio.IService.IProjectService;
import tn.esprit.portfolio.Repository.ProjectImageRepo;
import tn.esprit.portfolio.Repository.ProjectRepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    private final IProjectService projectService;
    private final ProjectRepo projectRepository;
    private final ProjectImageRepo projectImageRepository;

    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Project> createProject(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("technologies") String technologies,
            @RequestParam("images") List<MultipartFile> images) throws IOException {

        System.out.println("=== DÉBUT Création Projet ===");
        System.out.println("Titre: " + title);
        System.out.println("Description: " + description);
        System.out.println("Technologies: " + technologies);
        System.out.println("Nombre d'images: " + (images != null ? images.size() : 0));

        // Vérifier limite de fichiers
        if (images != null && images.size() > 400) {
            System.out.println("❌ Trop de fichiers. Limite = 20");
            return ResponseEntity.badRequest().body(null);
        }

        // 1. Créer le projet
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);
        project.setTechnologies(technologies);

        // 2. Sauvegarder le projet
        Project savedProject = projectService.createProject(project);
        System.out.println("✅ Projet sauvegardé avec ID: " + savedProject.getId());

        // 3. Sauvegarder les images si elles existent
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);

                if (file != null && !file.isEmpty()) {
                    System.out.println("📁 Traitement de l'image " + (i + 1) +
                            " - Nom: " + file.getOriginalFilename() +
                            " - Taille: " + file.getSize() + " bytes");

                    String filename = saveFile(file);

                    if (filename != null && !filename.isEmpty()) {
                        System.out.println("✅ Fichier sauvegardé: " + filename);

                        ProjectImage projectImage = new ProjectImage();
                        projectImage.setImageUrl(filename);
                        projectImage.setProject(savedProject);

                        ProjectImage savedImage = projectImageRepository.save(projectImage);
                        System.out.println("💾 ProjectImage sauvegardé - ID: " + savedImage.getId() +
                                " - URL: " + savedImage.getImageUrl() +
                                " - Project ID: " + savedImage.getProject().getId());

                        savedProject.getImages().add(savedImage);
                    }
                }
            }

            // Sauvegarder à nouveau le projet avec les images
            projectRepository.save(savedProject);
        }

        // 4. Récupérer le projet complet pour sérialisation JSON
        Project finalProject = projectRepository.findById(savedProject.getId())
                .orElse(savedProject);

        if (finalProject.getImages() != null) {
            finalProject.getImages().size(); // initialiser la collection
        }

        System.out.println("=== FIN Création Projet - Projet ID: " + finalProject.getId() + " ===");
        return ResponseEntity.ok(finalProject);
    }

    // 🔄 NOUVEAU: Endpoint pour mettre à jour un projet
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("technologies") String technologies,
            @RequestParam(value = "existingImages", required = false) String existingImagesJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {

        System.out.println("=== DÉBUT Mise à jour Projet ID: " + id + " ===");
        System.out.println("Titre: " + title);
        System.out.println("Description: " + description);
        System.out.println("Technologies: " + technologies);
        System.out.println("Images existantes JSON: " + existingImagesJson);
        System.out.println("Nouvelles images: " + (images != null ? images.size() : 0));

        // 1. Récupérer le projet existant
        Project project = projectService.getProjectById(id);

        // 2. Mettre à jour les champs de base
        project.setTitle(title);
        project.setDescription(description);
        project.setTechnologies(technologies);

        // 3. Gérer les images existantes à conserver
        List<ProjectImage> imagesToKeep = new ArrayList<>();

        if (existingImagesJson != null && !existingImagesJson.isEmpty()) {
            System.out.println("📋 Traitement des images existantes à conserver");

            // Parser le JSON des images existantes (format: [{"id":1,"imageUrl":"file.jpg"},...]
            // Vous pouvez utiliser Jackson ou Gson ici
            // Pour l'instant, on garde toutes les images existantes si le champ n'est pas vide
            imagesToKeep.addAll(project.getImages());
        }

        // 4. Supprimer les images qui ne sont plus dans la liste
        List<ProjectImage> imagesToDelete = new ArrayList<>();
        for (ProjectImage existingImage : project.getImages()) {
            boolean shouldKeep = imagesToKeep.stream()
                    .anyMatch(img -> img.getId().equals(existingImage.getId()));

            if (!shouldKeep) {
                imagesToDelete.add(existingImage);
                System.out.println("🗑️ Image à supprimer: " + existingImage.getImageUrl());
            }
        }

        // Supprimer les images de la base de données et du système de fichiers
        for (ProjectImage imageToDelete : imagesToDelete) {
            try {
                // Supprimer le fichier physique
                Path filePath = uploadDir.resolve(imageToDelete.getImageUrl()).normalize();
                Files.deleteIfExists(filePath);
                System.out.println("🗑️ Fichier supprimé: " + imageToDelete.getImageUrl());
            } catch (IOException e) {
                System.err.println("❌ Erreur lors de la suppression du fichier: " + e.getMessage());
            }

            project.getImages().remove(imageToDelete);
            projectImageRepository.delete(imageToDelete);
        }

        // 5. Ajouter les nouvelles images
        if (images != null && !images.isEmpty()) {
            System.out.println("📸 Ajout de " + images.size() + " nouvelles images");

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);

                if (file != null && !file.isEmpty()) {
                    System.out.println("📁 Traitement nouvelle image " + (i + 1) +
                            " - Nom: " + file.getOriginalFilename() +
                            " - Taille: " + file.getSize() + " bytes");

                    String filename = saveFile(file);

                    if (filename != null && !filename.isEmpty()) {
                        System.out.println("✅ Fichier sauvegardé: " + filename);

                        ProjectImage projectImage = new ProjectImage();
                        projectImage.setImageUrl(filename);
                        projectImage.setProject(project);

                        ProjectImage savedImage = projectImageRepository.save(projectImage);
                        System.out.println("💾 Nouvelle image sauvegardée - ID: " + savedImage.getId());

                        project.getImages().add(savedImage);
                    }
                }
            }
        }

        // 6. Sauvegarder le projet mis à jour
        Project updatedProject = projectRepository.save(project);

        // 7. Recharger le projet pour avoir toutes les données
        Project finalProject = projectRepository.findById(updatedProject.getId())
                .orElse(updatedProject);

        if (finalProject.getImages() != null) {
            finalProject.getImages().size(); // Force le chargement des images
        }

        System.out.println("=== FIN Mise à jour Projet ID: " + id + " - " +
                finalProject.getImages().size() + " images ===");
        return ResponseEntity.ok(finalProject);
    }

    // 💾 Sauvegarde fichier
    private String saveFile(MultipartFile file) throws IOException {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("📁 Répertoire créé: " + uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "image.jpg";
            }

            String cleanFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            String filename = System.currentTimeMillis() + "_" + cleanFilename;

            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            return filename;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde du fichier: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 📌 Tous les projets
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();

        List<ProjectDTO> dtos = projects.stream().map(project -> {
            ProjectDTO dto = new ProjectDTO();
            dto.setId(project.getId());
            dto.setTitle(project.getTitle());
            dto.setDescription(project.getDescription());
            dto.setTechnologies(project.getTechnologies());

            List<ProjectImageDTO> imageDTOs = project.getImages().stream()
                    .map(img -> new ProjectImageDTO(img.getId(), img.getImageUrl()))
                    .collect(Collectors.toList());
            dto.setImages(imageDTOs);

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 📌 Projet par ID
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    // 🗑️ Supprimer un projet
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        System.out.println("=== DÉBUT Suppression Projet ID: " + id + " ===");

        Project project = projectService.getProjectById(id);

        // Supprimer les fichiers images du système de fichiers
        if (project.getImages() != null) {
            for (ProjectImage image : project.getImages()) {
                try {
                    Path filePath = uploadDir.resolve(image.getImageUrl()).normalize();
                    Files.deleteIfExists(filePath);
                    System.out.println("🗑️ Fichier supprimé: " + image.getImageUrl());
                } catch (IOException e) {
                    System.err.println("❌ Erreur lors de la suppression du fichier: " + e.getMessage());
                }
            }
        }

        // Supprimer le projet (cascade supprimera les images de la DB)
        projectService.deleteProject(id);

        System.out.println("=== FIN Suppression Projet ID: " + id + " ===");
        return ResponseEntity.noContent().build();
    }

    // 🖼️ Servir une image
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path filePath = uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) contentType = "application/octet-stream";

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Test endpoint
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("{\"status\": \"API fonctionne\"}");
    }
}