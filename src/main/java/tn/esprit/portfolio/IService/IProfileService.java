package tn.esprit.portfolio.IService;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.portfolio.Entity.Profile;

import java.io.IOException;
import java.util.List;

public interface IProfileService {
    Profile createProfileWithPhoto(String fullName, String bio, String title, MultipartFile photoFile) throws IOException;

    List<Profile> getAllProfiles();

    Profile getProfileById(Long id);

    Profile updateProfile(Long id, Profile updatedProfile);

    void deleteProfile(Long id);
}
