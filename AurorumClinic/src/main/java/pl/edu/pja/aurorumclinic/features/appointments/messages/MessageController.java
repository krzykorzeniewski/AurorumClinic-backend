package pl.edu.pja.aurorumclinic.features.appointments.messages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Message;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.security.Principal;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat")
    @Transactional
    public void handle(@Valid MessageDto message,
                             Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        User receiver = userRepository.findById(message.receiverId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Message newMessage = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .sentAt(message.sentAt())
                .message(message.text())
                .build();
        messageRepository.save(newMessage);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(receiver.getId()),
                "/queue/messages", message);
    }

}
