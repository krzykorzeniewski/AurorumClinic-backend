package pl.edu.pja.aurorumclinic.shared.services;

import io.jsonwebtoken.io.Decoders;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.edu.pja.aurorumclinic.shared.data.TokenRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {TokenService.class})
@ActiveProfiles("test")
public class TokenServiceTest {

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    TokenRepository tokenRepository;

    @MockitoSpyBean
    TokenService spyTokenService;

    @Autowired
    TokenService tokenService;

    @Test
    void createRandomTokenShouldReturn32ByteString() {
        String result = tokenService.createRandomToken();

        assertThat(result).isNotNull();
        assertThat(Decoders.BASE64URL.decode(result)).hasSize(32);
    }

    @Test
    void createOtpShouldReturn6CharacterString() {
        String result = tokenService.createOtp();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(6);
    }

    @Test
    void createTokenShouldSaveTokenWithCorrectValues() {
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .build();
        String tokenValue = "superSafeNotDeterministicToken";
        TokenName name = TokenName.REFRESH;
        int minutesValid = 10;

        when(spyTokenService.createRandomToken()).thenReturn(tokenValue);
        when(passwordEncoder.encode(anyString())).thenReturn(tokenValue);

        tokenService.createToken(testUser, name, minutesValid);

        ArgumentCaptor<Token> tokenArgumentCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenArgumentCaptor.capture());
        verify(spyTokenService).deletePreviousTokens(testUser.getTokens(), name);
        verify(passwordEncoder).encode(tokenValue);
        Token savedToken = tokenArgumentCaptor.getValue();

        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        assertThat(savedToken.getName()).isEqualTo(name);
        assertThat(savedToken.getRawValue()).isEqualTo(tokenValue);
        assertThat(savedToken.getExpiryDate())
                .isCloseTo(LocalDateTime.now().plusMinutes(minutesValid), within(2, ChronoUnit.SECONDS));
    }

    @Test
    void createOtpTokenShouldSaveTokenWithCorrectValues() {
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .build();
        String tokenValue = "123123";
        TokenName name = TokenName.TWO_FACTOR_AUTH;
        int minutesValid = 5;

        when(spyTokenService.createOtp()).thenReturn(tokenValue);
        when(passwordEncoder.encode(anyString())).thenReturn(tokenValue);

        tokenService.createOtpToken(testUser, name, minutesValid);

        ArgumentCaptor<Token> tokenArgumentCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenArgumentCaptor.capture());
        verify(spyTokenService).deletePreviousTokens(testUser.getTokens(), name);
        verify(passwordEncoder).encode(tokenValue);
        Token savedToken = tokenArgumentCaptor.getValue();

        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        assertThat(savedToken.getName()).isEqualTo(name);
        assertThat(savedToken.getRawValue()).isEqualTo(tokenValue);
        assertThat(savedToken.getExpiryDate())
                .isCloseTo(LocalDateTime.now().plusMinutes(minutesValid), within(2, ChronoUnit.SECONDS));
    }

    @Test
    void validateAndDeleteTokenShouldThrowApiExceptionWhenTokenValueIsNotInUserTokens() {
        Token testToken = Token.builder()
                .value("some hashed value")
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .tokens(List.of(testToken))
                .build();

        String inputValue = "some hashed value not equal";

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> tokenService.validateAndDeleteToken(testUser, inputValue))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("Invalid");
        verify(passwordEncoder).matches(inputValue, testToken.getValue());
    }

    @Test
    void validateAndDeleteTokenShouldThrowApiExceptionWhenTokenIsExpired() {
        Token testToken = Token.builder()
                .value("some hashed value")
                .expiryDate(LocalDateTime.now().minusMinutes(10))
                .build();
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .tokens(List.of(testToken))
                .build();

        String inputValue = "some hashed value not equal";

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> tokenService.validateAndDeleteToken(testUser, inputValue))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("expired");
        verify(passwordEncoder).matches(inputValue, testToken.getValue());
    }

    @Test
    void validateAndDeleteTokenShouldDeleteTokenWithGivenValueForGivenUser() {
        Token testToken = Token.builder()
                .id(1L)
                .value("some hashed value")
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .tokens(List.of(testToken))
                .build();
        testToken.setUser(testUser);

        String inputValue = "some hashed value not equal";

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        tokenService.validateAndDeleteToken(testUser, inputValue);

        ArgumentCaptor<Token> tokenArgumentCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).delete(tokenArgumentCaptor.capture());

        Token deletedToken = tokenArgumentCaptor.getValue();
        assertThat(deletedToken.getId()).isEqualTo(testToken.getId());
        assertThat(deletedToken.getUser()).isEqualTo(testToken.getUser());
        assertThat(deletedToken.getValue()).isEqualTo(testToken.getValue());
        verify(passwordEncoder).matches(inputValue, testToken.getValue());
    }

    @Test
    void deletePreviousTokenShouldDoNothingWhenUserHasNoTokens() {
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .tokens(null)
                .build();

        assertThatNoException().isThrownBy(
                () -> tokenService.deletePreviousTokens(eq(testUser.getTokens()), any(TokenName.class)));
        verify(tokenRepository, times(0)).deleteAll(anyList());
    }

    @Test
    void deletePreviousTokensShouldDeleteAllTokensForUserWithGivenName() {
        Token testToken1 = Token.builder()
                .name(TokenName.REFRESH)
                .build();
        Token testToken2 = Token.builder()
                .name(TokenName.REFRESH)
                .build();
        Token testToken3 = Token.builder()
                .name(TokenName.EMAIL_UPDATE)
                .build();
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .tokens(List.of(testToken1, testToken2, testToken3))
                .build();

        tokenService.deletePreviousTokens(testUser.getTokens(), TokenName.REFRESH);

        ArgumentCaptor<List<Token>> deletedTokensArgumentCaptor = ArgumentCaptor.captor();

        verify(tokenRepository).deleteAll(deletedTokensArgumentCaptor.capture());

        List<Token> deletedTokens = deletedTokensArgumentCaptor.getValue();
        assertThat(deletedTokens).isNotEmpty();
        assertThat(deletedTokens).containsExactly(testToken1, testToken2);
    }
}
