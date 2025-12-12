package tn.esprit.portfolio.IService;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.portfolio.Entity.CvFile;

import java.io.IOException;
import java.util.List;

public interface ICvFileService {

    CvFile createCvFile(CvFile cvFile);

    List<CvFile> getAllCvFiles();

    CvFile getCvFileById(Long id);

    CvFile updateCvFile(Long id, CvFile updatedCvFile);

    void deleteCvFile(Long id);

    CvFile uploadCv(MultipartFile file) throws IOException;
}
