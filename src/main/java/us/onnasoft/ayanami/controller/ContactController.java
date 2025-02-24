package us.onnasoft.ayanami.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import us.onnasoft.ayanami.dto.ContactRequest;
import us.onnasoft.ayanami.dto.ContactResponse;
import us.onnasoft.ayanami.service.ContactService;

@RestController
@RequestMapping("/contact")
public class ContactController {

    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Handles contact form submissions.
     * 
     * @param request The contact form request data.
     * @param result  The result of validation.
     * @return ResponseEntity with status and message.
     */
    @PostMapping
    public ResponseEntity<ContactResponse> submitContactForm(@Valid @RequestBody ContactRequest request) {
        contactService.processContactForm(request);

        return ResponseEntity.ok().body(new ContactResponse(true, "Message sent successfully!"));
    }
}
