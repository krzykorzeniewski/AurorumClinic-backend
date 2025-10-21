package pl.edu.pja.aurorumclinic.features.newsletter.commands;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.newsletter.NewsletterMessageRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/newsletter")
@PreAuthorize("hasRole('ADMIN')")
public class CreateNewsletterMessage {

    @Value("${openai.chat.default-prompt}")
    private String defaultPrompt;
    private final OpenAiChatModel chatModel;
    private final NewsletterMessageRepository newsletterMessageRepository;

    @PostMapping("")
    public ResponseEntity<ApiResponse<CreateNewsletterMessageResponse>> createNewsletterMessage(
            @RequestParam(required = false) String prompt
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(prompt)));
    }

    private CreateNewsletterMessageResponse handle(String prompt) {
        CreateNewsletterMessagePromptResponse response = ChatClient.create(chatModel)
                .prompt()
                .user(u -> u.text(prompt != null ? prompt : defaultPrompt))
                .call()
                .entity(CreateNewsletterMessagePromptResponse.class);

        NewsletterMessage newMessage = new NewsletterMessage();
        newMessage.setCreatedAt(LocalDateTime.now());
        newMessage.setApproved(false);
        newMessage.setText(response.text);
        newMessage.setSubject(response.subject);
        NewsletterMessage savedMessage = newsletterMessageRepository.save(newMessage);

        return CreateNewsletterMessageResponse.builder()
                .id(savedMessage.getId())
                .text(savedMessage.getText())
                .createdAt(savedMessage.getCreatedAt())
                .subject(savedMessage.getSubject())
                .approved(savedMessage.isApproved())
                .build();
    }

    @Builder
    record CreateNewsletterMessageResponse(Long id,
                                           String subject,
                                           String text,
                                           LocalDateTime createdAt,
                                           boolean approved) {
    }

    record CreateNewsletterMessagePromptResponse(String subject, String text) {
    }

}
