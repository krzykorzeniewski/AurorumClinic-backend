package pl.edu.pja.aurorumclinic.features.appointments.messages;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MessageController {

    @MessageMapping("/messages/{receiverId}")
    @SendTo("/queue/messages/{receiverId}")
    public String handle(@DestinationVariable("receiverId") Long receiverId) {
        return "tak" + receiverId;
    }

}
