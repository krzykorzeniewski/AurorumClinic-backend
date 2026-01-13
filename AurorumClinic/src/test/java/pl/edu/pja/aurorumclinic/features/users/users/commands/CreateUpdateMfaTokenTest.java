package pl.edu.pja.aurorumclinic.features.users.users.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.users.users.events.MfaTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = {CreateUpdateMfaToken.class},
        properties = {"2fa-update-token.expiration.minutes=15"}
)
@ActiveProfiles("test")
@RecordApplicationEvents
class CreateUpdateMfaTokenTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;

    @Autowired
    CreateUpdateMfaToken controller;

    @Test
    void createMfaUpdateTokenShouldThrowApiNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.createMfaUpdateToken(userId))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(tokenService);
    }

    @Test
    void createMfaUpdateTokenShouldThrowApiExceptionWhenPhoneNumberNotVerified() {
        Long userId = 1L;

        User userFromDb = User.builder()
                .id(userId)
                .phoneNumberVerified(false)
                .twoFactorAuth(false)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));

        assertThatThrownBy(() -> controller.createMfaUpdateToken(userId))
                .isExactlyInstanceOf(ApiException.class)
                .hasMessageContaining("Phone number is not verified");

        verify(userRepository).findById(userId);
        verifyNoInteractions(tokenService);
    }

    @Test
    void createMfaUpdateTokenShouldThrowApiExceptionWhenTwoFactorAuthAlreadyEnabled() {
        Long userId = 1L;

        User userFromDb = User.builder()
                .id(userId)
                .phoneNumberVerified(true)
                .twoFactorAuth(true)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));

        assertThatThrownBy(() -> controller.createMfaUpdateToken(userId))
                .isExactlyInstanceOf(ApiException.class)
                .hasMessageContaining("already has 2fa enabled");

        verify(userRepository).findById(userId);
        verifyNoInteractions(tokenService);
    }

    @Test
    void createMfaUpdateTokenShouldCreateTokenAndPublishEvent(
            @Autowired ApplicationEvents applicationEvents
    ) {
        Long userId = 1L;

        User userFromDb = User.builder()
                .id(userId)
                .phoneNumberVerified(true)
                .twoFactorAuth(false)
                .build();

        Token token = Token.builder()
                .id(10L)
                .name(TokenName.TWO_FACTOR_AUTH_UPDATE)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));
        when(tokenService.createOtpToken(eq(userFromDb), eq(TokenName.TWO_FACTOR_AUTH_UPDATE), eq(15)))
                .thenReturn(token);

        controller.createMfaUpdateToken(userId);

        verify(userRepository).findById(userId);
        verify(tokenService).createOtpToken(userFromDb, TokenName.TWO_FACTOR_AUTH_UPDATE, 15);

        assertThat(applicationEvents.stream(MfaTokenCreatedEvent.class))
                .filteredOn(event ->
                        Objects.equals(readEventValue(event, "user"), userFromDb)
                                && Objects.equals(readEventValue(event, "token"), token)
                )
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
