package us.onnasoft.ayanami.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.onnasoft.ayanami.utils.TemplateRenderer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.sender.email}")
    private String senderEmail;

    @Autowired
    private TemplateRenderer templateRenderer;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sends a plain text email (Not recommended for HTML content)
     *
     * @param to      The recipient's email address.
     * @param subject The subject of the email.
     * @param text    The plain text content of the email.
     */
    public void sendEmail(String to, String subject, String text) {
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("from", senderEmail);
        emailData.put("to", new String[]{to});
        emailData.put("subject", subject);
        emailData.put("text", text);

        try {
            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(emailData),
                    MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(RESEND_API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to send email: " + response.body().string());
            }

            System.out.println("Email sent successfully!");
        } catch (IOException e) {
            throw new RuntimeException("Error sending email", e);
        }
    }

    /**
     * Sends an HTML email using a Thymeleaf template.
     *
     * @param to           The recipient's email address.
     * @param subject      The subject of the email.
     * @param templateName The name of the Thymeleaf template (without the .html extension).
     * @param model        A map containing variables to inject into the template.
     */
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> model) {
        // Render the Thymeleaf template to HTML content
        String htmlContent = templateRenderer.renderTemplate(templateName, model);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("from", senderEmail);
        emailData.put("to", new String[]{to});
        emailData.put("subject", subject);
        emailData.put("html", htmlContent);

        try {
            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(emailData),
                    MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(RESEND_API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to send email: " + response.body().string());
            }

            System.out.println("HTML Email sent successfully!");
        } catch (IOException e) {
            throw new RuntimeException("Error sending HTML email", e);
        }
    }
}
