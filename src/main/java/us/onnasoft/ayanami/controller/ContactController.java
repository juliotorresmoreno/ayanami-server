package us.onnasoft.ayanami.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import us.onnasoft.ayanami.dto.ContactRequest;
import us.onnasoft.ayanami.service.ContactService;

import java.util.HashMap;
import java.util.Map;

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
     * @param result The result of validation.
     * @return ResponseEntity with status and message.
     */
    @PostMapping
    public ResponseEntity<?> submitContactForm(@Valid @RequestBody ContactRequest request, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        contactService.processContactForm(request);

        return ResponseEntity.ok().body(Map.of("success", true, "message", "Message sent successfully!"));
    }
}
