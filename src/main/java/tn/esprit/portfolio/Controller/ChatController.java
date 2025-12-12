package tn.esprit.portfolio.Controller;// ChatController.java

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:4200") // À adapter pour la production
public class ChatController {

    @Value("${anthropic.api.key}")
    private String anthropicApiKey;

    @Value("${anthropic.api.url}")
    private String anthropicApiUrl;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatRequest request) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", anthropicApiKey);
            headers.set("anthropic-version", "2023-06-01");

            // Corps de la requête
            Map<String, Object> body = new HashMap<>();
            body.put("model", "claude-3-sonnet-20240229");
            body.put("max_tokens", 1000);

            // Construction du message avec le contexte
            String systemPrompt = """
                Tu es un assistant IA pour un portfolio professionnel. 
                Voici les informations sur la personne:
                
                Nom: Votre Nom
                Profession: Développeur Full Stack
                Compétences: Java, Spring Boot, React, Node.js, PostgreSQL, MongoDB
                Projets:
                - E-commerce Platform: Application complète avec Spring Boot et React
                - Task Manager: Système de gestion de tâches avec authentification
                - Portfolio Website: Site personnel avec blog intégré
                Expérience: 3 ans de développement web
                Formation: Master en Informatique
                Langues: Français, Anglais, Arabe
                
                Réponds de manière professionnelle, concise et engageante aux questions. 
                Si on te demande des informations qui ne sont pas dans le contexte, 
                indique poliment que tu n'as pas cette information mais suggère de 
                contacter directement via le formulaire de contact.
                """;

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", systemPrompt + "\n\nQuestion: " + request.getMessage());

            body.put("messages", new Object[]{userMessage});

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Envoi à l'API Claude
            ResponseEntity<Map> response = restTemplate.exchange(
                    anthropicApiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Extraction de la réponse
            Map<String, Object> responseBody = response.getBody();
            // Logique d'extraction du texte de la réponse selon la structure de l'API Claude

            Map<String, String> chatResponse = new HashMap<>();
            chatResponse.put("response", "Réponse de l'IA");

            return ResponseEntity.ok(chatResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la communication avec l'IA"));
        }
    }

    // Classe interne pour la requête
    public static class ChatRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}