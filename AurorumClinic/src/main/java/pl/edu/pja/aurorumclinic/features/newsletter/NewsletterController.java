package pl.edu.pja.aurorumclinic.features.newsletter;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final PatientRepository patientRepository;
    private final OpenAiChatModel chatModel;
    private final NewsletterMessageRepository newsletterMessageRepository;

    @Value("${openai.chat.default-prompt}")
    private String defaultPrompt;

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<CreateNewsletterMessageResponse>> createNewsletterMessage(
            @RequestParam String prompt) {
        NewsletterMessage newsletterMessage = ChatClient.create(chatModel)
                .prompt()
                .user(u -> u.text(prompt != null ? prompt : defaultPrompt))
                .call()
                .entity(NewsletterMessage.class);
        newsletterMessage.setCreatedAt(LocalDateTime.now());
        NewsletterMessage savedMessage = newsletterMessageRepository.save(newsletterMessage);
        return ResponseEntity.ok(ApiResponse.success(CreateNewsletterMessageResponse.builder()
                        .id(savedMessage.getId())
                        .text(savedMessage.getText())
                        .createdAt(savedMessage.getCreatedAt())
                        .approved(savedMessage.isApproved())
                .build()));
    }

    // TODO put

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/subscribe")
    @Transactional
    public ResponseEntity<?> subscribe(@RequestBody @Valid SubscribeRequest subscribeRequest) {
        Patient patientFromDb = patientRepository.findByEmail(subscribeRequest.email());
        if (patientFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        if (patientFromDb.isNewsletter()) {
            throw new ApiException("Email is already subscribed to newsletter", "email");
        }
        patientFromDb.setNewsletter(true);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/unsubscribe")
    @Transactional
    public ResponseEntity<?> unsubscribe(@RequestBody @Valid UnsubscribeRequest unsubscribeRequest) {
        Patient patientFromDb = patientRepository.findByEmail(unsubscribeRequest.email());
        if (patientFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        if (!patientFromDb.isNewsletter()) {
            throw new ApiException("Email is not subscribed to newsletter", "email");
        }
        patientFromDb.setNewsletter(false);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Builder
    record CreateNewsletterMessageResponse(Long id,
                                           String text,
                                           LocalDateTime createdAt,
                                           boolean approved) {
    }

}
