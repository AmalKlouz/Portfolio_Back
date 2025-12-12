package tn.esprit.portfolio.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.portfolio.Entity.ContactMessage;
import tn.esprit.portfolio.IService.IContactMessageService;
import tn.esprit.portfolio.Repository.ContactMessageRepo;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageRepo contactMessageRepo;

    private final IContactMessageService contactMessageService; // Pas ContactMessageRepo !

    @PostMapping
    public ResponseEntity<ContactMessage> createMessage(@RequestBody ContactMessage message) {
        ContactMessage savedMessage = contactMessageService.createMessage(message);
        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping
    public List<ContactMessage> getAllMessages() {
        return contactMessageRepo.findAll();
    }

    @GetMapping("/{id}")
    public ContactMessage getMessageById(@PathVariable Long id) {
        return contactMessageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
    }

    @DeleteMapping("/{id}")
    public String deleteMessage(@PathVariable Long id) {
        ContactMessage message = contactMessageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
        contactMessageRepo.delete(message);
        return "Message deleted successfully";
    }
}
