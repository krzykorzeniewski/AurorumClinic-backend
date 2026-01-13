package pl.edu.pja.aurorumclinic.features.chats.commands;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.MessageRepository;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Message;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {SendMessage.class})
@ActiveProfiles("test")
class SendMessageTest {

    @MockitoBean
    MessageRepository messageRepository;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    SendMessage sendMessage;

    private Principal principalWithId(Long id) {
        return () -> String.valueOf(id);
    }

    private Object newRequest(String text, LocalDateTime sentAt, Long receiverId) throws Exception {
        Class<?> clazz = Class.forName(
                "pl.edu.pja.aurorumclinic.features.chats.commands.SendMessage$SendMessageRequest"
        );
        Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, LocalDateTime.class, Long.class);
        ctor.setAccessible(true);
        return ctor.newInstance(text, sentAt, receiverId);
    }

    private void invokeHandle(Object request, Principal principal) {
        try {
            Method handle = SendMessage.class.getMethod("handle", request.getClass(), Principal.class);
            handle.invoke(sendMessage, request, principal);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object invokeGetter(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getMethod(methodName);
            return m.invoke(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldSendMessageWhenAppointmentExists() throws Exception {
        Long senderId = 1L;
        Long receiverId = 2L;

        LocalDateTime sentAt = LocalDateTime.of(2026, 1, 12, 21, 47, 54, 870391700);
        Object req = newRequest("Cześć, jak się czujesz?", sentAt, receiverId);

        User sender = User.builder().id(senderId).build();
        User receiver = User.builder().id(receiverId).build();

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(appointmentRepository.existsBetweenUsers(senderId, receiverId)).thenReturn(true);

        invokeHandle(req, principalWithId(senderId));

        verify(messageRepository).save(argThat((Message m) ->
                m.getSender().equals(sender) &&
                        m.getReceiver().equals(receiver) &&
                        m.getText().equals("Cześć, jak się czujesz?") &&
                        m.getSentAt().equals(sentAt)
        ));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        verify(simpMessagingTemplate).convertAndSendToUser(
                eq(String.valueOf(receiverId)),
                eq("/queue/messages"),
                payloadCaptor.capture()
        );

        Object payload = payloadCaptor.getValue();
        assertThat(payload).isNotNull();
        assertThat(payload.getClass().getName())
                .isEqualTo("pl.edu.pja.aurorumclinic.features.chats.commands.SendMessage$SendMessageResponse");

        assertThat(invokeGetter(payload, "text")).isEqualTo("Cześć, jak się czujesz?");
        assertThat(invokeGetter(payload, "sentAt")).isEqualTo(sentAt);

        // DOSTOSOWANE DO AKTUALNEJ IMPLEMENTACJI:
        // SendMessageResponse(receiverId, authorId) jest tworzone jako (senderId, receiverId)
        assertThat(invokeGetter(payload, "receiverId")).isEqualTo(senderId);
        assertThat(invokeGetter(payload, "authorId")).isEqualTo(receiverId);
    }

    @Test
    void shouldThrowNotFoundWhenSenderDoesNotExist() throws Exception {
        Long senderId = 1L;
        Long receiverId = 2L;

        Object req = newRequest("tekst", LocalDateTime.of(2026, 1, 12, 10, 0), receiverId);

        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invokeHandle(req, principalWithId(senderId)))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(senderId);
        verifyNoInteractions(appointmentRepository, messageRepository, simpMessagingTemplate);
    }

    @Test
    void shouldThrowNotFoundWhenReceiverDoesNotExist() throws Exception {
        Long senderId = 1L;
        Long receiverId = 2L;

        Object req = newRequest("tekst", LocalDateTime.of(2026, 1, 12, 10, 0), receiverId);

        User sender = User.builder().id(senderId).build();
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invokeHandle(req, principalWithId(senderId)))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(senderId);
        verify(userRepository).findById(receiverId);
        verifyNoInteractions(appointmentRepository, messageRepository, simpMessagingTemplate);
    }

    @Test
    void shouldThrowApiExceptionWhenNoAppointmentBetweenUsers() throws Exception {
        Long senderId = 1L;
        Long receiverId = 2L;

        Object req = newRequest("tekst", LocalDateTime.of(2026, 1, 12, 10, 0), receiverId);

        User sender = User.builder().id(senderId).build();
        User receiver = User.builder().id(receiverId).build();

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(appointmentRepository.existsBetweenUsers(senderId, receiverId)).thenReturn(false);

        assertThatThrownBy(() -> invokeHandle(req, principalWithId(senderId)))
                .isExactlyInstanceOf(ApiException.class)
                .hasMessageContaining("No appointment exists between users");

        verify(appointmentRepository).existsBetweenUsers(senderId, receiverId);
        verifyNoInteractions(messageRepository, simpMessagingTemplate);
    }
}
