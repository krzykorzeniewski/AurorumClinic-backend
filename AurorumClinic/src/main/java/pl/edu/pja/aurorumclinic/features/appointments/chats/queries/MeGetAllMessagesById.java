package pl.edu.pja.aurorumclinic.features.appointments.chats.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.chats.MessageRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;


@RestController
@RequestMapping("/api/messages/me")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
public class MeGetAllMessagesById {

    private final MessageRepository messageRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Page<GetMessageResponse>>> getAllMessagesWithUserId(
            @PathVariable("id") Long recipientId,
            @AuthenticationPrincipal Long myId,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(handle(recipientId, myId, pageable)));
    }

    public Page<GetMessageResponse> handle(Long recipientId, Long myId, Pageable pageable) {
        return messageRepository.findAllMessagesBetween(myId, recipientId, pageable);
    }

}
