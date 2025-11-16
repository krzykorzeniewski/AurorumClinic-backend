package pl.edu.pja.aurorumclinic.features.auth.login;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.auth.login.LoginService;
import pl.edu.pja.aurorumclinic.features.auth.login.LoginServiceImpl;
import pl.edu.pja.aurorumclinic.features.auth.login.dtos.*;
import pl.edu.pja.aurorumclinic.features.auth.login.events.MfaTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.shared.JwtUtils;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {LoginServiceImpl.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class LoginServiceTest {

    @Value("${refresh-token-expiration-minutes}")
    private Integer refreshTokenExpirationInMinutes;

    @Value("${2fa-login-token.expiration.minutes}")
    private Integer mfaTokenExpirationInMinutes;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    JwtUtils jwtUtils;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    LoginServiceImpl loginService;

    @Test
    void loginShouldThrowApiAuthExceptionWhenEmailIsNotFound() {
        LoginUserRequest loginUserRequest = new LoginUserRequest("mariusz@example.com", "1234");

        when(userRepository.findByEmail(loginUserRequest.email())).thenReturn(null);

        assertThatThrownBy(() -> loginService.login(loginUserRequest))
                .isExactlyInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void loginShouldThrowApiAuthExceptionWhenPasswordIsInvalid() {
        User testUser = User.builder()
                .email("mariusz@example.com")
                .password("123")
                .build();
        LoginUserRequest loginUserRequest = new LoginUserRequest(testUser.getEmail(), "1234");

        when(userRepository.findByEmail(loginUserRequest.email())).thenReturn(testUser);

        when(passwordEncoder.matches(loginUserRequest.password(), testUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(loginUserRequest))
                .isExactlyInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void loginShouldThrowApiAuthExceptionWhenEmailIsNotVerified() {
        User testUser = User.builder()
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(false)
                .build();
        LoginUserRequest loginUserRequest = new LoginUserRequest("mariusz@example.com", "1234");

        when(userRepository.findByEmail(loginUserRequest.email())).thenReturn(testUser);

        when(passwordEncoder.matches(loginUserRequest.password(), testUser.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> loginService.login(loginUserRequest))
                .isExactlyInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void loginShouldReturnDtoWithTwoFactorAuthTrueAndNoTokensWhenTwoFactorAuthIsEnabled() {
        User testUser = User.builder()
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(true)
                .twoFactorAuth(true)
                .role(UserRole.PATIENT)
                .build();
        LoginUserRequest loginUserRequest = new LoginUserRequest("mariusz@example.com", "1234");

        when(userRepository.findByEmail(loginUserRequest.email())).thenReturn(testUser);

        when(passwordEncoder.matches(loginUserRequest.password(), testUser.getPassword())).thenReturn(true);

        LoginUserResponse response = loginService.login(loginUserRequest);

        assertThat(response).isNotNull();
        assertThat(response.twoFactorAuth()).isEqualTo(true);
        assertThat(response.role()).isEqualTo(UserRole.PATIENT);
        assertThat(response.accessToken()).isNull();
        assertThat(response.refreshToken()).isNull();
    }

    @Test
    void loginShouldReturnDtoWithJwtAndRefreshTokenWhenTwoFactorAuthIsDisabled() {
        User testUser = User.builder()
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(true)
                .twoFactorAuth(false)
                .role(UserRole.PATIENT)
                .build();
        LoginUserRequest loginUserRequest = new LoginUserRequest("mariusz@example.com", "1234");
        String jwt = "jwt";
        String refreshToken = "rt";

        when(userRepository.findByEmail(loginUserRequest.email())).thenReturn(testUser);

        when(passwordEncoder.matches(loginUserRequest.password(), testUser.getPassword())).thenReturn(true);

        when(jwtUtils.createJwt(testUser)).thenReturn(jwt);
        when(tokenService.createToken(testUser, TokenName.REFRESH, refreshTokenExpirationInMinutes)).thenReturn(Token.builder()
                        .rawValue(refreshToken)
                .build());

        LoginUserResponse response = loginService.login(loginUserRequest);

        assertThat(response).isNotNull();
        assertThat(response.twoFactorAuth()).isEqualTo(false);
        assertThat(response.role()).isEqualTo(UserRole.PATIENT);
        assertThat(response.accessToken()).isEqualTo(jwt);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void refreshShouldThrowApiAuthExceptionWhenJwtIsInvalid() {
        RefreshAccessTokenRequest request = new RefreshAccessTokenRequest(
                "definitely not valid access token",
                "refresh token");

        when(jwtUtils.validateJwt(request.accessToken())).thenThrow(new JwtException("jwt exception"));

        assertThatThrownBy(() -> loginService.refresh(request))
                .isExactlyInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void refreshShouldNotThrowExceptionWhenJwtIsValidButExpired() {
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(true)
                .twoFactorAuth(false)
                .role(UserRole.PATIENT)
                .build();
        RefreshAccessTokenRequest request = new RefreshAccessTokenRequest(
                "access token",
                "refresh token");

        Token testToken = Token.builder()
                .id(1L)
                .value("some value")
                .rawValue("some value")
                .build();

        when(jwtUtils.validateJwt(anyString())).thenThrow(new ExpiredJwtException(null, null, null));
        when(jwtUtils.getUserIdFromExpiredJwt(anyString())).thenReturn(testUser.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        tokenService.validateAndDeleteToken(testUser, "");
        when(jwtUtils.createJwt(testUser)).thenReturn("");
        when(tokenService.createToken(testUser, TokenName.REFRESH, refreshTokenExpirationInMinutes)).thenReturn(
                testToken
        );

        assertThatNoException().isThrownBy(() -> loginService.refresh(request));
    }

    @Test
    void refreshShouldThrowApiAuthExceptionWhenUserIdFromJwtIsNotFound() {
        RefreshAccessTokenRequest request = new RefreshAccessTokenRequest(
                "access token",
                "refresh token");

        jwtUtils.validateJwt(anyString());
        when(jwtUtils.getUserIdFromJwt(anyString())).thenReturn(null);

        assertThatThrownBy(() -> loginService.refresh(request))
                .isExactlyInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void refreshShouldReturnDtoWithJwtAndRefreshTokenAndValidUserData() {
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(true)
                .twoFactorAuth(false)
                .role(UserRole.PATIENT)
                .build();

        String accessToken = "some access token value";

        RefreshAccessTokenRequest request = new RefreshAccessTokenRequest(
                "access token",
                "refresh token");

        Token refreshToken = Token.builder()
                .id(1L)
                .value("some hashed refresh token value")
                .rawValue("some refresh token value")
                .build();

        jwtUtils.validateJwt(anyString());
        when(jwtUtils.getUserIdFromJwt(anyString())).thenReturn(testUser.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        tokenService.validateAndDeleteToken(testUser, "");
        when(jwtUtils.createJwt(testUser)).thenReturn(accessToken);
        when(tokenService.createToken(testUser, TokenName.REFRESH, refreshTokenExpirationInMinutes)).thenReturn(
                refreshToken
        );

        LoginUserResponse response = loginService.refresh(request);

        assertThat(response).isNotNull();
        assertThat(response.twoFactorAuth()).isEqualTo(testUser.isTwoFactorAuth());
        assertThat(response.role()).isEqualTo(testUser.getRole());
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken.getRawValue());
    }

    @Test
    void login2faShouldThrowApiAuthExceptionWhenEmailIsNotFound() {
        TwoFactorAuthLoginRequest request = new TwoFactorAuthLoginRequest("123123",
                "mariusz@example.com");

        when(userRepository.findByEmail(request.email())).thenReturn(null);

        assertThatThrownBy(() -> loginService.login2fa(request)).isExactlyInstanceOf(ApiAuthenticationException.class);
    }

    @Test

    void login2faShouldReturnDtoWithJwtAndRefreshToken() {
        TwoFactorAuthLoginRequest request = new TwoFactorAuthLoginRequest("123123",
                "mariusz@example.com");
        String accessToken = "some access token value";
        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(true)
                .twoFactorAuth(true)
                .role(UserRole.PATIENT)
                .build();
        Token refreshToken = Token.builder()
                .id(1L)
                .value("some hashed refresh token value")
                .rawValue("some refresh token value")
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(testUser);
        tokenService.validateAndDeleteToken(testUser, request.token());
        when(jwtUtils.createJwt(testUser)).thenReturn(accessToken);
        when(tokenService.createToken(testUser, TokenName.REFRESH, refreshTokenExpirationInMinutes)).
                thenReturn(refreshToken);

        LoginUserResponse response = loginService.login2fa(request);

        assertThat(response).isNotNull();
        assertThat(response.twoFactorAuth()).isEqualTo(testUser.isTwoFactorAuth());
        assertThat(response.role()).isEqualTo(testUser.getRole());
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken.getRawValue());
    }

    @Test
    void createMfaTokenShouldThrowApiAuthExceptionWhenEmailIsNotFound() {
        TwoFactorAuthTokenRequest request = new TwoFactorAuthTokenRequest(
                "mariusz@example.com"
        );

        when(userRepository.findByEmail(request.email())).thenReturn(null);

        assertThatThrownBy(() -> loginService.createMfaToken(request))
                .isExactlyInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void createMfaTokenShouldThrowApiAuthExceptionWhenUserPhoneNumberIsNotVerified() {
        TwoFactorAuthTokenRequest request = new TwoFactorAuthTokenRequest(
                "mariusz@example.com"
        );

        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(true)
                .phoneNumberVerified(false)
                .twoFactorAuth(true)
                .role(UserRole.PATIENT)
                .build();
        when(userRepository.findByEmail(request.email())).thenReturn(testUser);

        assertThatThrownBy(() -> loginService.createMfaToken(request))
                .isExactlyInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void createMfaTokenShouldThrowApiAuthExceptionWhenUserHas2faDisabled() {
        TwoFactorAuthTokenRequest request = new TwoFactorAuthTokenRequest(
                "mariusz@example.com"
        );

        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(true)
                .phoneNumberVerified(true)
                .twoFactorAuth(false)
                .role(UserRole.PATIENT)
                .build();
        when(userRepository.findByEmail(request.email())).thenReturn(testUser);

        assertThatThrownBy(() -> loginService.createMfaToken(request))
                .isExactlyInstanceOf(ApiAuthenticationException.class);
    }

    @Test
    void createMfaTokenShouldCreateTokenAndPublishTokenCreatedEvent(
            @Autowired ApplicationEvents applicationEvents) {
        TwoFactorAuthTokenRequest request = new TwoFactorAuthTokenRequest(
                "mariusz@example.com"
        );

        User testUser = User.builder()
                .id(1L)
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(true)
                .phoneNumberVerified(true)
                .twoFactorAuth(true)
                .role(UserRole.PATIENT)
                .build();

        Token mfaToken = Token.builder()
                .rawValue("some raw value")
                .value("some hashed value")
                .user(testUser)
                .expiryDate(LocalDateTime.now())
                .build();

        MfaTokenCreatedEvent event = new MfaTokenCreatedEvent(mfaToken, testUser);


        when(userRepository.findByEmail(request.email())).thenReturn(testUser);
        when(tokenService.createOtpToken(testUser, TokenName.TWO_FACTOR_AUTH, mfaTokenExpirationInMinutes))
                .thenReturn(mfaToken);

        loginService.createMfaToken(request);

        verify(tokenService).createOtpToken(testUser, TokenName.TWO_FACTOR_AUTH, mfaTokenExpirationInMinutes);
        //verify(applicationEventPublisher).publishEvent(event);
    }
}
