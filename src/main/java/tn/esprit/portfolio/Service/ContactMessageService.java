package tn.esprit.portfolio.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.portfolio.Entity.ContactMessage;
import tn.esprit.portfolio.IService.IContactMessageService;
import tn.esprit.portfolio.Repository.ContactMessageRepo;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactMessageService implements IContactMessageService {

    private final ContactMessageRepo contactMessageRepository;
    private final EmailService emailService;

    @Override
    public ContactMessage createMessage(ContactMessage message) {
        ContactMessage savedMessage = contactMessageRepository.save(message);

        // Envoi de l'email après sauvegarde
        try {
            emailService.sendContactNotification(savedMessage);
        } catch (Exception e) {
            log.error("Failed to send email notification, but message was saved", e);
            // Le message est quand même sauvegardé même si l'email échoue
        }

        return savedMessage;
    }
    @Override
    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAll();
    }

    @Override
    public ContactMessage getMessageById(Long id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
    }

    @Override
    public void deleteMessage(Long id) {
        ContactMessage message = getMessageById(id);
        contactMessageRepository.delete(message);
    }
}
