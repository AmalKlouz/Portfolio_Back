package tn.esprit.portfolio.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String fullName;


    @Column(length = 2000)
    private String bio; // court paragraphe de presentation


    private String title; // e.g. Ingenieure en Genie Logiciel


    private String photoUrl; // url vers la photo
}