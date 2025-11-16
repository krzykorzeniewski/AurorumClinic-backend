package pl.edu.pja.aurorumclinic.features.auth.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.auth.login.LoginService;
import pl.edu.pja.aurorumclinic.features.auth.login.LoginServiceImpl;
import pl.edu.pja.aurorumclinic.features.auth.login.dtos.LoginUserRequest;
import pl.edu.pja.aurorumclinic.features.auth.login.dtos.LoginUserResponse;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.shared.JwtUtils;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import static org.assertj.core.api.Assertions.*;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = {LoginServiceImpl.class})
@ActiveProfiles("test")
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
}
