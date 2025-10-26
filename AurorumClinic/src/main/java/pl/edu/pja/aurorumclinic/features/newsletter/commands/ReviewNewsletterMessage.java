package pl.edu.pja.aurorumclinic.features.newsletter.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.newsletter.shared.NewsletterMessageRepository;
import pl.edu.pja.aurorumclinic.features.newsletter.events.NewsletterMessageApprovedEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/newsletter/messages")
@PreAuthorize("hasRole('ADMIN')")
public class ReviewNewsletterMessage {

    private final UserRepository userRepository;
    private final NewsletterMessageRepository newsletterMessageRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<?>> reviewNewsletterMessage(@PathVariable("id") Long newsletterMessId,
                                                                  @RequestBody @Valid UpdateNewsletterMessageRequest request,
                                                                  @AuthenticationPrincipal Long adminId) {
       handle(newsletterMessId, adminId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long newsletterMessId, Long adminId, UpdateNewsletterMessageRequest request) {
        User reviewer = userRepository.findById(adminId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "adminId")
        );
        NewsletterMessage newsletterMessFromDb = newsletterMessageRepository.findById(newsletterMessId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "newsletterMessageId")
        );
        newsletterMessFromDb.setText(request.text);
        newsletterMessFromDb.setApproved(request.approved);
        newsletterMessFromDb.setSubject(request.subject);
        newsletterMessFromDb.setReviewer(reviewer);
        newsletterMessFromDb.setScheduledAt(request.scheduledAt);
        newsletterMessFromDb.setReviewedAt(LocalDateTime.now());

        if (request.approved) {
            applicationEventPublisher.publishEvent(new NewsletterMessageApprovedEvent(newsletterMessFromDb));
        }
    }

    record UpdateNewsletterMessageRequest(@NotEmpty @Size(max = 500) String text,
                                          @NotEmpty @Size(max = 100) String subject,
                                          @NotNull Boolean approved,
                                          LocalDateTime scheduledAt) {
    }
}
