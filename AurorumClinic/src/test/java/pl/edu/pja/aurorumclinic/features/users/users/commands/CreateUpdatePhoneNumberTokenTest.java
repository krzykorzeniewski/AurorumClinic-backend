package pl.edu.pja.aurorumclinic.features.users.users.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.users.users.events.UpdatePhoneNumberTokenCreatedEvent;
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
        classes = {CreateUpdatePhoneNumberToken.class},
        properties = {"phone-number-verification-token.expiration.minutes=15"}
)
@ActiveProfiles("test")
@RecordApplicationEvents
class CreateUpdatePhoneNumberTokenTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;

    @Autowired
    CreateUpdatePhoneNumberToken controller;

    @Test
    void createUpdatePhoneNumberTokenShouldThrowApiNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;
        var request = new CreateUpdatePhoneNumberToken.UpdateUserPhoneNumberTokenRequest("123456789");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.createUpdatePhoneNumberToken(userId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(tokenService);
    }

    @Test
    void createUpdatePhoneNumberTokenShouldThrowApiConflictExceptionWhenPhoneNumberAlreadyTaken() {
        Long userId = 1L;
        String newNumber = "123456789";
        var request = new CreateUpdatePhoneNumberToken.UpdateUserPhoneNumberTokenRequest(newNumber);

        User userFromDb = User.builder()
                .id(userId)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(true);

        assertThatThrownBy(() -> controller.createUpdatePhoneNumberToken(userId, request))
                .isExactlyInstanceOf(ApiConflictException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).existsByPhoneNumber(newNumber);
        verifyNoInteractions(tokenService);
    }

    @Test
    void createUpdatePhoneNumberTokenShouldSetPendingPhoneNumberCreateTokenAndPublishEvent(
            @Autowired ApplicationEvents applicationEvents
    ) {
        Long userId = 1L;
        String newNumber = "123456789";
        var request = new CreateUpdatePhoneNumberToken.UpdateUserPhoneNumberTokenRequest(newNumber);

        User userFromDb = User.builder()
                .id(userId)
                .build();

        Token token = Token.builder()
                .id(10L)
                .name(TokenName.PHONE_NUMBER_UPDATE)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);

        when(tokenService.createOtpToken(eq(userFromDb), eq(TokenName.PHONE_NUMBER_UPDATE), eq(15)))
                .thenReturn(token);

        controller.createUpdatePhoneNumberToken(userId, request);

        assertThat(userFromDb.getPendingPhoneNumber()).isEqualTo(newNumber);

        verify(userRepository).findById(userId);
        verify(userRepository).existsByPhoneNumber(newNumber);
        verify(tokenService).createOtpToken(userFromDb, TokenName.PHONE_NUMBER_UPDATE, 15);

        assertThat(applicationEvents.stream(UpdatePhoneNumberTokenCreatedEvent.class))
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
