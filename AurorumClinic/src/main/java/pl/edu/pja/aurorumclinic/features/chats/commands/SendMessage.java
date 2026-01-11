package pl.edu.pja.aurorumclinic.features.chats.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.data.MessageRepository;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Message;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class SendMessage {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat")
    @Transactional
    public void handle(@Valid SendMessageRequest message,
                             Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        User receiver = userRepository.findById(message.receiverId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!appointmentRepository.existsBetweenUsers(sender.getId(), receiver.getId())) {
            throw new ApiException("No appointment exists between users", "senderId");
        }
        Message newMessage = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .sentAt(message.sentAt())
                .text(message.text())
                .build();
        SendMessageResponse res = new SendMessageResponse(
                message.text(),
                message.sentAt(),
                message.receiverId(),
                sender.getId()
        );
        messageRepository.save(newMessage);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(receiver.getId()),
                "/queue/messages", res);
    }

    record SendMessageRequest(@NotEmpty @Size(max = 500) String text,
                              @NotNull LocalDateTime sentAt,
                              @NotNull Long receiverId) {
    }

    record SendMessageResponse(@NotEmpty @Size(max = 500) String text,
                               @NotNull LocalDateTime sentAt,
                               @NotNull Long receiverId,
                               @NotNull Long authorId) {
    }
}
