package tn.esprit.portfolio.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.portfolio.Entity.CvFile;
import tn.esprit.portfolio.Repository.CvFileRepo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/cvfiles")
@RequiredArgsConstructor
public class CvFileController {

    private final CvFileRepo cvFileRepository;

    @PostMapping
    public ResponseEntity<CvFile> uploadCv(@RequestParam("file") MultipartFile file) throws IOException {
        // Créer le dossier uploads si nécessaire
        String uploadDir = "uploads/cvs/";
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) uploadFolder.mkdirs();

        // Sauvegarder le fichier
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());

        // Créer l'entité CvFile
        CvFile cvFile = new CvFile();
        cvFile.setFilename(fileName);
        cvFile.setContentType(file.getContentType());
        cvFile.setSize(file.getSize());
        cvFile.setStoragePath(filePath.toString());

        CvFile savedCv = cvFileRepository.save(cvFile);
        return ResponseEntity.ok(savedCv);
    }
    @GetMapping
    public List<CvFile> getAllCvFiles() {
        return cvFileRepository.findAll();
    }

    @GetMapping("/{id}")
    public CvFile getCvFileById(@PathVariable Long id) {
        return cvFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV File not found with id: " + id));
    }

    @PutMapping("/{id}")
    public CvFile updateCvFile(@PathVariable Long id, @RequestBody CvFile updatedCvFile) {
        CvFile cvFile = cvFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV File not found with id: " + id));

        cvFile.setFilename(updatedCvFile.getFilename());
        cvFile.setContentType(updatedCvFile.getContentType());
        cvFile.setSize(updatedCvFile.getSize());
        cvFile.setStoragePath(updatedCvFile.getStoragePath());

        return cvFileRepository.save(cvFile);
    }

    @DeleteMapping("/{id}")
    public String deleteCvFile(@PathVariable Long id) {
        CvFile cvFile = cvFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV File not found with id: " + id));
        cvFileRepository.delete(cvFile);
        return "CV File deleted successfully";
    }
    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<byte[]> downloadCv(@PathVariable String filename) throws IOException {
        Path path = Paths.get("uploads/cvs/" + filename);

        if (!Files.exists(path)) {
            throw new RuntimeException("Fichier non trouvé: " + filename);
        }

        byte[] fileBytes = Files.readAllBytes(path);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .body(fileBytes);
    }


}
