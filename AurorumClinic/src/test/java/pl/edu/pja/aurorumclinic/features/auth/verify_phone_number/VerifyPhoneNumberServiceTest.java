package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.auth.verify_phone_number.events.PhoneNumberVerificationTokenCreatedEvent;
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
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {VerifyPhoneNumberServiceImpl.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class VerifyPhoneNumberServiceTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;

    @Value("${phone-number-verification-token.expiration.minutes}")
    private Integer phoneNumberVerificationTokenExpirationInMinutes;

    @Autowired
    VerifyPhoneNumberServiceImpl verifyPhoneNumberService;

    @Test
    void createPhoneNumberVerificationTokenShouldThrowApiNotFoundExceptionWhenIdIsNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> verifyPhoneNumberService.createPhoneNumberVerificationToken(1L))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void createPhoneNumberVerificationTokenShouldThrowApiExceptionWhenPhoneNumberIsVerified() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(User.builder()
                        .phoneNumberVerified(true)
                .build()));
        assertThatThrownBy(() -> verifyPhoneNumberService.createPhoneNumberVerificationToken(1L))
                .isExactlyInstanceOf(ApiException.class);
    }

    @Test
    void createPhoneNumberVerificationTokenShouldCreateTokenAndPublishEventWhenDataIsCorrect(
            @Autowired ApplicationEvents applicationEvents
            ) {
        User testUser = User.builder()
                .id(1L)
                .phoneNumberVerified(false)
                .build();
        Token testToken = Token.builder()
                .name(TokenName.PHONE_NUMBER_VERIFICATION)
                .value("some hashed value")
                .rawValue("some raw value")
                .build();
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(tokenService.createOtpToken(any(User.class), any(TokenName.class), anyInt())).thenReturn(testToken);

        verifyPhoneNumberService.createPhoneNumberVerificationToken(testUser.getId());

        verify(tokenService).createOtpToken(testUser, TokenName.PHONE_NUMBER_VERIFICATION,
                phoneNumberVerificationTokenExpirationInMinutes);

        assertThat(applicationEvents.stream(PhoneNumberVerificationTokenCreatedEvent.class))
                .filteredOn(event ->
                        Objects.equals(event.user(), testUser) && Objects.equals(event.token(), testToken))
                .hasSize(1);
    }

    @Test
    void verifyPhoneNumberShouldThrowApiNotFoundExceptionWhenIdIsNotFound() {
        VerifyPhoneNumberRequest request = new VerifyPhoneNumberRequest("123123");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyPhoneNumberService.verifyPhoneNumber(request, 1L))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void verifyPhoneNumberShouldSetPhoneNumberVerifiedWhenDataIsCorrect() {
        VerifyPhoneNumberRequest request = new VerifyPhoneNumberRequest("123123");
        User testUser = User.builder()
                .id(1L)
                .phoneNumberVerified(false)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        tokenService.validateAndDeleteToken(any(User.class), anyString());

        verifyPhoneNumberService.verifyPhoneNumber(request, testUser.getId());

        verify(tokenService).validateAndDeleteToken(testUser, request.token());
        assertThat(testUser.isPhoneNumberVerified()).isEqualTo(true);
    }

}
