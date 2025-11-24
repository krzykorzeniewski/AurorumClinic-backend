package pl.edu.pja.aurorumclinic.features.newsletter.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.newsletter.shared.NewsletterMessageRepository;
import pl.edu.pja.aurorumclinic.features.newsletter.queries.shared.GetNewsletterMessageResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;

@RestController
@RequestMapping("/api/newsletter/messages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GetAllNewsletterMessages {

    private final NewsletterMessageRepository newsletterMessageRepository;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<GetNewsletterMessageResponse>>> getAllNewsletterMessages(
            @PageableDefault Pageable pageable
            ) {
        return ResponseEntity.ok(ApiResponse.success(handle(pageable)));
    }

    private Page<GetNewsletterMessageResponse> handle(Pageable pageable) {
        Page<NewsletterMessage> newsletterMessFromDb = newsletterMessageRepository.findAll(pageable);

        return newsletterMessFromDb.map(
                newsletterMessage -> GetNewsletterMessageResponse.builder()
                        .createdAt(newsletterMessage.getCreatedAt())
                        .subject(newsletterMessage.getSubject())
                        .text(newsletterMessage.getText())
                        .approved(newsletterMessage.isApproved())
                        .reviewedAt(newsletterMessage.getReviewedAt())
                        .sentAt(newsletterMessage.getSentAt())
                        .scheduledAt(newsletterMessage.getScheduledAt())
                        .reviewer(newsletterMessage.getReviewer() == null
                                ? null : GetNewsletterMessageResponse.ReviewerDto.builder()
                                .id(newsletterMessage.getReviewer().getId())
                                .name(newsletterMessage.getReviewer().getName())
                                .surname(newsletterMessage.getReviewer().getSurname())
                                .build())
                        .build()
        );
    }


}
