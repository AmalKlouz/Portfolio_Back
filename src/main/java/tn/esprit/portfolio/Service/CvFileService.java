package tn.esprit.portfolio.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.portfolio.Entity.CvFile;
import tn.esprit.portfolio.IService.ICvFileService;
import tn.esprit.portfolio.Repository.CvFileRepo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class CvFileService implements ICvFileService {

    private final CvFileRepo cvFileRepository;
    private static final String UPLOAD_DIR = "uploads/cv/";
@Override
public CvFile saveOrUpdateCv(MultipartFile file) throws IOException {
        // Créer le dossier si nécessaire
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Vérifier s'il y a déjà un CV
        CvFile existingCv = getCurrentCv();

        // Supprimer l'ancien fichier s'il existe
        if (existingCv != null && existingCv.getStoragePath() != null) {
            try {
                Path oldFile = Paths.get(existingCv.getStoragePath());
                Files.deleteIfExists(oldFile);
                log.info("Ancien CV supprimé: {}", existingCv.getFilename());
            } catch (IOException e) {
                log.error("Erreur lors de la suppression de l'ancien CV", e);
            }
            // Supprimer l'entrée de la base
            cvFileRepository.delete(existingCv);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String newFilename = System.currentTimeMillis() + "_" + originalFilename;
        String storagePath = UPLOAD_DIR + newFilename;

        // Sauvegarder le fichier
        Path destination = Paths.get(storagePath);
        Files.copy(file.getInputStream(), destination);

        // Créer et sauvegarder l'entité
        CvFile cvFile = new CvFile();
        cvFile.setFilename(originalFilename);
        cvFile.setContentType(file.getContentType());
        cvFile.setSize(file.getSize());
        cvFile.setStoragePath(storagePath);

        return cvFileRepository.save(cvFile);
    }

    public CvFile getCurrentCv() {
        // Récupérer le premier CV (il n'y en a qu'un)
        return (CvFile) cvFileRepository.findFirstByOrderByIdDesc()
                .orElse(null);
    }

    public void deleteCv() throws IOException {
        CvFile cvFile = getCurrentCv();
        if (cvFile != null) {
            // Supprimer le fichier physique
            if (cvFile.getStoragePath() != null) {
                Files.deleteIfExists(Paths.get(cvFile.getStoragePath()));
            }
            // Supprimer de la base
            cvFileRepository.delete(cvFile);
        }
    }

    public byte[] getCvFileBytes() throws IOException {
        CvFile cvFile = getCurrentCv();
        if (cvFile == null || cvFile.getStoragePath() == null) {
            return null;
        }
        return Files.readAllBytes(Paths.get(cvFile.getStoragePath()));
    }

    @Override
    public CvFile createCvFile(CvFile cvFile) {
        return cvFileRepository.save(cvFile);
    }

    @Override
    public List<CvFile> getAllCvFiles() {
        return cvFileRepository.findAll();
    }

    @Override
    public CvFile getCvFileById(Long id) {
        return cvFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV File not found with id: " + id));
    }

    @Override
    public CvFile updateCvFile(Long id, CvFile updatedCvFile) {
        CvFile cvFile = getCvFileById(id);

        cvFile.setFilename(updatedCvFile.getFilename());
        cvFile.setContentType(updatedCvFile.getContentType());
        cvFile.setSize(updatedCvFile.getSize());
        cvFile.setStoragePath(updatedCvFile.getStoragePath());

        return cvFileRepository.save(cvFile);
    }

    @Override
    public void deleteCvFile(Long id) {
        CvFile cvFile = getCvFileById(id);
        cvFileRepository.delete(cvFile);
    }
    @Override
    public CvFile uploadCv(MultipartFile file) throws IOException {
        String uploadDir = "uploads/cvs/";
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) uploadFolder.mkdirs();

        // Générer un nom unique pour éviter les doublons
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());

        CvFile cvFile = new CvFile();
        cvFile.setFilename(fileName);
        cvFile.setContentType(file.getContentType());
        cvFile.setSize(file.getSize());
        cvFile.setStoragePath(filePath.toString());

        return cvFileRepository.save(cvFile);
    }

}
