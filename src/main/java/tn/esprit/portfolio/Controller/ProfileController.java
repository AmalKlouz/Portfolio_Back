package tn.esprit.portfolio.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.portfolio.Entity.Profile;
import tn.esprit.portfolio.Repository.ProfileRepo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileRepo profileRepository;
    private static final String UPLOAD_DIR = "uploads/";

    // Méthode pour sauvegarder une photo
    private String savePhoto(MultipartFile photoFile) throws IOException {
        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        // Générer un nom unique
        String fileName = System.currentTimeMillis() + "_" +
                (photoFile.getOriginalFilename() != null ?
                        photoFile.getOriginalFilename() : "photo");

        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.write(filePath, photoFile.getBytes());

        return fileName;
    }

    // Méthode pour supprimer une photo
    private void deletePhoto(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            try {
                Path photoPath = Paths.get(UPLOAD_DIR + fileName);
                Files.deleteIfExists(photoPath);
            } catch (IOException e) {
                log.warn("Impossible de supprimer la photo {}: {}", fileName, e.getMessage());
            }
        }
    }


    @PostMapping()
    public ResponseEntity<Profile> createProfile(
            @RequestParam String fullName,
            @RequestParam String bio,
            @RequestParam String title,
            @RequestParam("photo") MultipartFile photoFile
    ) throws IOException {

        // Créer le dossier uploads si nécessaire
        String uploadDir = "uploads/";
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) uploadFolder.mkdirs();

        // Sauvegarder la photo
        String fileName = photoFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, photoFile.getBytes());

        // Créer le Profile
        Profile profile = new Profile();
        profile.setFullName(fullName);
        profile.setBio(bio);
        profile.setTitle(title);
        profile.setPhotoUrl(fileName);

        Profile savedProfile = profileRepository.save(profile);
        return ResponseEntity.ok(savedProfile);
    }

    @GetMapping
    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    @GetMapping("/{id}")
    public Profile getProfileById(@PathVariable Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Profile> updateProfile(
            @PathVariable Long id,
            @RequestPart(value = "profile", required = false) Profile profileData,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile
    ) throws IOException {

        Profile existingProfile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

        // Mettre à jour les champs texte
        if (profileData != null) {
            existingProfile.setFullName(profileData.getFullName());
            existingProfile.setBio(profileData.getBio());
            existingProfile.setTitle(profileData.getTitle());
        }

        // Mettre à jour la photo si fournie
        if (photoFile != null && !photoFile.isEmpty()) {
            // Supprimer l'ancienne photo
            if (existingProfile.getPhotoUrl() != null) {
                deletePhoto(existingProfile.getPhotoUrl());
            }

            // Sauvegarder la nouvelle
            String fileName = savePhoto(photoFile);
            existingProfile.setPhotoUrl(fileName);
        }

        Profile updatedProfile = profileRepository.save(existingProfile);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{id}")
    public String deleteProfile(@PathVariable Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
        profileRepository.delete(profile);
        return "Profile deleted successfully";
    }
    @GetMapping("/photo/{filename:.+}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String filename) throws IOException {
        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        Path filePath = uploadDir.resolve(filename);

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Photo non trouvée: " + filename);
        }

        byte[] photoBytes = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                .body(photoBytes);
    }


}
