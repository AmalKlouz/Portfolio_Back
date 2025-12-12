package tn.esprit.portfolio.Config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class TomcatConfig {

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers((Connector connector) -> {
            connector.setMaxParameterCount(5000); // nombre de parties (fichiers + champs)
            connector.setMaxPostSize(200 * 1024 * 1024); // taille max POST totale
            // nombre de parties (champs + fichiers)
        });
        return factory;
    }
}
