package tn.esprit.portfolio.IService;

import tn.esprit.portfolio.Entity.ContactMessage;

import java.util.List;

public interface IContactMessageService {

    ContactMessage createMessage(ContactMessage message);

    List<ContactMessage> getAllMessages();

    ContactMessage getMessageById(Long id);

    void deleteMessage(Long id);
}
