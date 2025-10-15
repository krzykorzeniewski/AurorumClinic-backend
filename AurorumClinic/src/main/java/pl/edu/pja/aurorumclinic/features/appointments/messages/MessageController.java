package pl.edu.pja.aurorumclinic.features.appointments.messages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Message;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @MessageMapping("/chat/{receiverId}")
    @SendTo("/user/{receiverId}/queue/messages")
    @Transactional
    public MessageDto handle(@Valid MessageDto message,
                       @DestinationVariable("receiverId") Long receiverId) {
        User sender = userRepository.findById(message.senderId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Message newMessage = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .sentAt(message.sentAt())
                .message(message.text())
                .build();
        messageRepository.save(newMessage);
        return message;
    }

}
