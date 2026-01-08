package pl.edu.pja.aurorumclinic.features.newsletter.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.newsletter.queries.shared.GetNewsletterMessageResponse;
import pl.edu.pja.aurorumclinic.features.newsletter.shared.NewsletterMessageRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/newsletter/messages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GetNewsletterMessageById {

    private final NewsletterMessageRepository newsletterMessageRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetNewsletterMessageResponse>> getNewsletterMessageById(
            @PathVariable("id") Long newsletterMessId
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(newsletterMessId)));
    }

    private GetNewsletterMessageResponse handle(Long newsletterMessId) {
        NewsletterMessage newsletterMessFromDb = newsletterMessageRepository.findById(newsletterMessId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );

        return GetNewsletterMessageResponse.builder()
                        .id(newsletterMessFromDb.getId())
                        .createdAt(newsletterMessFromDb.getCreatedAt())
                        .subject(newsletterMessFromDb.getSubject())
                        .text(newsletterMessFromDb.getText())
                        .approved(newsletterMessFromDb.isApproved())
                        .reviewedAt(newsletterMessFromDb.getReviewedAt())
                        .sentAt(newsletterMessFromDb.getSentAt())
                        .scheduledAt(newsletterMessFromDb.getScheduledAt())
                        .reviewer(newsletterMessFromDb.getReviewer() == null
                                ? null : GetNewsletterMessageResponse.ReviewerDto.builder()
                                .id(newsletterMessFromDb.getReviewer().getId())
                                .name(newsletterMessFromDb.getReviewer().getName())
                                .surname(newsletterMessFromDb.getReviewer().getSurname())
                                .build())
                        .build();
    }
}
