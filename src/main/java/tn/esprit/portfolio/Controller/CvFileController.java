package tn.esprit.portfolio.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.portfolio.Entity.CvFile;
import tn.esprit.portfolio.Repository.CvFileRepo;
import tn.esprit.portfolio.Service.CvFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/cvfiles")
@RequiredArgsConstructor
public class CvFileController {

    private final CvFileRepo cvFileRepository;
    private final CvFileService cvService;

    // POST /api/cvfiles - Upload ou mise à jour du CV
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadOrUpdateCv(@RequestParam("file") MultipartFile file) {
        try {
            log.info("=== CV UPLOAD START ===");
            log.info("File name: {}", file.getOriginalFilename());
            log.info("File size: {}", file.getSize());
            log.info("Content type: {}", file.getContentType());

            if (file.isEmpty()) {
                log.error("File is empty");
                return ResponseEntity.badRequest().body("Le fichier est vide");
            }

            String contentType = file.getContentType();
            if (contentType == null) {
                log.error("Content type is null");
                return ResponseEntity.badRequest().body("Type de fichier non détecté");
            }

            Set<String> allowedTypes = Set.of(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "image/jpeg",
                    "image/png",
                    "image/jpg"
            );

            if (!allowedTypes.contains(contentType.toLowerCase())) {
                log.error("Content type not allowed: {}", contentType);
                return ResponseEntity.badRequest()
                        .body("Format de fichier non supporté. Formats acceptés: PDF, DOC, DOCX, JPG, PNG");
            }

            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                log.error("File too large: {} bytes", file.getSize());
                return ResponseEntity.badRequest()
                        .body("Le fichier est trop volumineux (max 10MB)");
            }

            CvFile savedCv = cvService.saveOrUpdateCv(file);
            log.info("=== CV UPLOAD SUCCESS ===");
            return ResponseEntity.ok(savedCv);

        } catch (IOException e) {
            log.error("IOException during file upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement du fichier: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during file upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur: " + e.getMessage());
        }
    }

    // GET /api/cvfiles/current - Récupérer le CV actuel
    @GetMapping("/current")
    public ResponseEntity<CvFile> getCurrentCv() {
        CvFile cvFile = cvService.getCurrentCv();
        if (cvFile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cvFile);
    }

    // GET /api/cvfiles/all - Récupérer tous les CVs
    @GetMapping("/all")
    public ResponseEntity<List<CvFile>> getAllCvFiles() {
        List<CvFile> cvFiles = cvFileRepository.findAll();
        return ResponseEntity.ok(cvFiles);
    }

    // GET /api/cvfiles/{id} - Récupérer un CV par ID
    @GetMapping("/{id}")
    public ResponseEntity<CvFile> getCvFileById(@PathVariable Long id) {
        return cvFileRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/cvfiles/download - Télécharger le CV actuel
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadCurrentCv() throws IOException {
        CvFile cvFile = cvService.getCurrentCv();
        if (cvFile == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileBytes = cvService.getCvFileBytes();
        ByteArrayResource resource = new ByteArrayResource(fileBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cvFile.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(cvFile.getContentType()))
                .contentLength(cvFile.getSize())
                .body(resource);
    }

    // GET /api/cvfiles/download/{filename} - Télécharger un CV par nom de fichier
    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<byte[]> downloadCvByFilename(@PathVariable String filename) throws IOException {
        Path path = Paths.get("uploads/cvs/" + filename);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileBytes = Files.readAllBytes(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(fileBytes);
    }

    // GET /api/cvfiles/exists - Vérifier si un CV existe
    @GetMapping("/exists")
    public ResponseEntity<Boolean> cvExists() {
        CvFile cvFile = cvService.getCurrentCv();
        return ResponseEntity.ok(cvFile != null);
    }

    // PUT /api/cvfiles/{id} - Mettre à jour un CV
    @PutMapping("/{id}")
    public ResponseEntity<CvFile> updateCvFile(@PathVariable Long id, @RequestBody CvFile updatedCvFile) {
        return cvFileRepository.findById(id)
                .map(cvFile -> {
                    cvFile.setFilename(updatedCvFile.getFilename());
                    cvFile.setContentType(updatedCvFile.getContentType());
                    cvFile.setSize(updatedCvFile.getSize());
                    cvFile.setStoragePath(updatedCvFile.getStoragePath());
                    return ResponseEntity.ok(cvFileRepository.save(cvFile));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/cvfiles - Supprimer le CV actuel
    @DeleteMapping
    public ResponseEntity<Void> deleteCurrentCv() throws IOException {
        CvFile cvFile = cvService.getCurrentCv();
        if (cvFile == null) {
            return ResponseEntity.notFound().build();
        }
        cvService.deleteCv();
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/cvfiles/{id} - Supprimer un CV par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCvFile(@PathVariable Long id) {
        return cvFileRepository.findById(id)
                .map(cvFile -> {
                    cvFileRepository.delete(cvFile);
                    return ResponseEntity.ok("CV supprimé avec succès");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}