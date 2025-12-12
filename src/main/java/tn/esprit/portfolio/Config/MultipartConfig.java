package tn.esprit.portfolio.Config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(5));       // 5MB par fichier
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));  // 100MB pour toute la requête
        factory.setFileSizeThreshold(DataSize.ofKilobytes(0)); // seuil avant écriture disque
        return factory.createMultipartConfig();
    }
}
