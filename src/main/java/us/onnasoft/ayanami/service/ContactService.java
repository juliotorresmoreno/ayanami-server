package us.onnasoft.ayanami.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.onnasoft.ayanami.dto.ContactRequest;

import java.util.HashMap;
import java.util.Map;

@Service
public class ContactService {

    @Value("${owner_email}")
    private String ownerEmail;

    @Autowired
    private EmailService emailService;

    /**
     * Processes the contact form submission.
     * 
     * @param request The contact form request data.
     */
    public void processContactForm(ContactRequest request) {
        Map<String, Object> model = new HashMap<>();
        model.put("username", request.getName());
        model.put("email", request.getEmail());
        model.put("message", request.getMessage());

        emailService.sendTemplateEmail(
          ownerEmail,
          "New Contact Form Submission",
          "contact-notification",
          model
        );
    }
}
