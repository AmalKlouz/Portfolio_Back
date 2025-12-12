package tn.esprit.portfolio.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.portfolio.Entity.Profile;
import tn.esprit.portfolio.IService.IProfileService;
import tn.esprit.portfolio.Repository.ProfileRepo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {

    private final ProfileRepo profileRepository;
@Override
public Profile createProfileWithPhoto(String fullName, String bio, String title, MultipartFile photoFile) throws IOException {
        String uploadDir = "uploads/";
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) uploadFolder.mkdirs();

        // Générer un nom unique pour éviter doublons
        String fileName = System.currentTimeMillis() + "_" + photoFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, photoFile.getBytes());

        Profile profile = new Profile();
        profile.setFullName(fullName);
        profile.setBio(bio);
        profile.setTitle(title);
    profile.setPhotoUrl(fileName);

        return profileRepository.save(profile);
    }
    @Override
    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }
@Override
public Profile getProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
    }
@Override
public Profile updateProfile(Long id, Profile updatedProfile) {
        Profile profile = getProfileById(id);

        profile.setFullName(updatedProfile.getFullName());
        profile.setBio(updatedProfile.getBio());
        profile.setTitle(updatedProfile.getTitle());
        profile.setPhotoUrl(updatedProfile.getPhotoUrl());

        return profileRepository.save(profile);
    }
@Override
public void deleteProfile(Long id) {
        Profile profile = getProfileById(id);
        profileRepository.delete(profile);
    }
}
