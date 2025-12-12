package tn.esprit.portfolio.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PutMapping("/{id}")
    public Profile updateProfile(@PathVariable Long id, @RequestBody Profile updatedProfile) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

        profile.setFullName(updatedProfile.getFullName());
        profile.setBio(updatedProfile.getBio());
        profile.setTitle(updatedProfile.getTitle());
        profile.setPhotoUrl(updatedProfile.getPhotoUrl());

        return profileRepository.save(profile);
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
