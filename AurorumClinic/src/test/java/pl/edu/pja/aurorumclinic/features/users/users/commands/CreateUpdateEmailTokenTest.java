package pl.edu.pja.aurorumclinic.features.users.users.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.users.users.events.UpdateEmailTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = {CreateUpdateEmailToken.class},
        properties = {"email-verification-token.expiration.minutes=15"}
)
@ActiveProfiles("test")
@RecordApplicationEvents
class CreateUpdateEmailTokenTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;

    @Autowired
    CreateUpdateEmailToken controller;

    @Test
    void createUpdateEmailTokenShouldThrowApiNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;
        var request = new CreateUpdateEmailToken.UpdateUserEmailTokenRequest("new.email@example.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.createUpdateEmailToken(userId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(tokenService);
    }

    @Test
    void createUpdateEmailTokenShouldThrowApiConflictExceptionWhenEmailAlreadyTaken() {
        Long userId = 1L;
        String newEmail = "taken@example.com";
        var request = new CreateUpdateEmailToken.UpdateUserEmailTokenRequest(newEmail);

        User userFromDb = User.builder()
                .id(userId)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> controller.createUpdateEmailToken(userId, request))
                .isExactlyInstanceOf(ApiConflictException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(newEmail);
        verifyNoInteractions(tokenService);
    }

    @Test
    void createUpdateEmailTokenShouldSetPendingEmailCreateTokenAndPublishEvent(
            @Autowired ApplicationEvents applicationEvents
    ) {
        Long userId = 1L;
        String newEmail = "new.email@example.com";
        var request = new CreateUpdateEmailToken.UpdateUserEmailTokenRequest(newEmail);

        User userFromDb = User.builder()
                .id(userId)
                .email("old.email@example.com")
                .build();

        Token token = Token.builder()
                .id(10L)
                .name(TokenName.EMAIL_UPDATE)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        when(tokenService.createOtpToken(eq(userFromDb), eq(TokenName.EMAIL_UPDATE), eq(15)))
                .thenReturn(token);

        controller.createUpdateEmailToken(userId, request);

        assertThat(userFromDb.getPendingEmail()).isEqualTo(newEmail);

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(newEmail);
        verify(tokenService).createOtpToken(userFromDb, TokenName.EMAIL_UPDATE, 15);

        assertThat(applicationEvents.stream(UpdateEmailTokenCreatedEvent.class))
                .filteredOn(event ->
                        Objects.equals(readEventValue(event, "user"), userFromDb)
                                && Objects.equals(readEventValue(event, "token"), token))
                .hasSize(1);
    }

    private static Object readEventValue(Object event, String name) {
        try {
            try {
                var m = event.getClass().getMethod(name);
                return m.invoke(event);
            } catch (NoSuchMethodException ignored) {
                String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                var m = event.getClass().getMethod(getter);
                return m.invoke(event);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Nie mogę odczytać '" + name + "' z eventu: " + event.getClass().getName(), e
            );
        }
    }
}
