package tn.esprit.portfolio.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cv_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CvFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String filename;


    private String contentType;


    private Long size;


    private String storagePath; // chemin local ou cloud
}
