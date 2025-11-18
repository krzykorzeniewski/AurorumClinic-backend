package pl.edu.pja.aurorumclinic.features.auth.reset_password;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.auth.reset_password.events.ResetPasswordTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.PasswordValidator;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

import java.util.Objects;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ResetPasswordServiceImpl.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class ResetPasswordServiceTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    PasswordValidator passwordValidator;

    @Autowired
    ResetPasswordServiceImpl resetPasswordService;

    @Value("${reset-password-token.expiration.minutes}")
    private Integer resetPasswordTokenExpirationInMinutes;

    @Test
    void createResetPasswordTokenShouldDoNothingWhenEmailIsNotFound() {
        ResetPasswordTokenRequest request = new ResetPasswordTokenRequest("mariusz@example.com");

        when(userRepository.findByEmail(request.email())).thenReturn(null);

        assertThatNoException().isThrownBy(() -> resetPasswordService.createResetPasswordToken(request));
        verify(tokenService, times(0)).createToken(any(User.class), any(TokenName.class), anyInt());
    }

    @Test
    void createResetPasswordTokenShouldDoNothingWhenEmailIstNotVerified() {
        ResetPasswordTokenRequest request = new ResetPasswordTokenRequest("mariusz@example.com");

        when(userRepository.findByEmail(request.email())).thenReturn(User.builder()
                        .emailVerified(false)
                .build());

        assertThatNoException().isThrownBy(() -> resetPasswordService.createResetPasswordToken(request));
        verify(tokenService, times(0)).createToken(any(User.class), any(TokenName.class), anyInt());
    }

    @Test
    void createResetPasswordTokenShouldCreateTokenAndPublishEventWhenDataIsCorrect(
            @Autowired ApplicationEvents applicationEvents
            ) {
        ResetPasswordTokenRequest request = new ResetPasswordTokenRequest("mariusz@example.com");
        User testUser = User.builder()
                .email("mariusz@example.com")
                .emailVerified(true)
                .build();
        Token testToken = Token.builder()
                .name(TokenName.PASSWORD_RESET)
                .value("some hashed value")
                .rawValue("some raw value")
                .build();
        when(userRepository.findByEmail(request.email())).thenReturn(testUser);
        when(tokenService.createToken(any(User.class), any(TokenName.class), anyInt())).thenReturn(testToken);

        resetPasswordService.createResetPasswordToken(request);
        verify(tokenService).createToken(testUser, TokenName.PASSWORD_RESET, resetPasswordTokenExpirationInMinutes);
        assertThat(applicationEvents.stream(ResetPasswordTokenCreatedEvent.class))
                .filteredOn(event ->
                        Objects.equals(event.user(), testUser) && Objects.equals(event.token(), testToken))
                .hasSize(1);
    }

    @Test
    void resetPasswordShouldThrowApiNotFoundExceptionWhenEmailIsNotFound() {
        ResetPasswordRequest request = new ResetPasswordRequest("SecretPass12345", "SecretToken12345",
                "mariusz@example.com");

        when(userRepository.findByEmail(request.email())).thenReturn(null);

        assertThatThrownBy(() -> resetPasswordService.resetPassword(request))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void resetPasswordShouldSetNewPasswordWhenDataIsCorrect() {
        ResetPasswordRequest request = new ResetPasswordRequest("SecretPass12345", "SecretToken12345",
                "mariusz@example.com");
        User testUser = User.builder()
                .email(request.email())
                .password("somepassword")
                .build();
        when(userRepository.findByEmail(request.email())).thenReturn(testUser);
        passwordValidator.validatePassword(request.password());
        tokenService.validateAndDeleteToken(testUser, request.token());
        when(passwordEncoder.encode(request.password())).then(invocation -> invocation.getArgument(0));

        resetPasswordService.resetPassword(request);

        assertThat(testUser.getPassword()).isEqualTo(request.password());

    }
}
