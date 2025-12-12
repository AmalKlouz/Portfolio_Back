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
