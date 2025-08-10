package pl.edu.pja.aurorumclinic.users.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.pja.aurorumclinic.models.Patient;
import pl.edu.pja.aurorumclinic.models.User;
import pl.edu.pja.aurorumclinic.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.security.SecurityUtils;
import pl.edu.pja.aurorumclinic.security.exceptions.ExpiredRefreshTokenException;
import pl.edu.pja.aurorumclinic.security.exceptions.InvalidAccessTokenException;
import pl.edu.pja.aurorumclinic.security.exceptions.RefreshTokenNotFoundException;
import pl.edu.pja.aurorumclinic.users.UserRepository;
import pl.edu.pja.aurorumclinic.users.dtos.*;
import pl.edu.pja.aurorumclinic.users.shared.EmailNotUniqueException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserServiceImpl userService;

    RegisterEmployeeRequestDto registerEmployeeRequestDto;
    RegisterPatientRequestDto registerPatientRequestDto;
    LoginUserRequestDto loginUserRequestDto;
    RefreshTokenRequestDto refreshTokenRequestDto;
    Authentication authentication;
    User testUser;

    private AutoCloseable closeable;


    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        registerEmployeeRequestDto = new RegisterEmployeeRequestDto("Jan", "Kowalski",
                "00000000000", LocalDate.now(), "kowalski@pm.me", "abc123", "123123123");
        registerPatientRequestDto = new RegisterPatientRequestDto("Jan", "Kowalski",
                "00000000000", LocalDate.now(), "kowalski@pm.me", "abc123", "123123123");
        loginUserRequestDto = new LoginUserRequestDto("kowalski@pm.me", "abc123");
        testUser = new User();
        authentication = new UsernamePasswordAuthenticationToken(
                "kowalski@pm.me", null, List.of(new SimpleGrantedAuthority(UserRole.PATIENT.name()))
        );
        refreshTokenRequestDto = new RefreshTokenRequestDto("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOi" +
                "JodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok",
                "m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void registerEmployeeShouldThrowExceptionWhenEmailExistsInDb() {
        when(userRepository.findByEmail(registerEmployeeRequestDto.email())).thenReturn(testUser);
        assertThrowsExactly(EmailNotUniqueException.class, () -> userService.registerEmployee(registerEmployeeRequestDto));
    }

    @Test
    void registerEmployeeShouldReturnUserObjectWithProperlyPopulatedFields() throws EmailNotUniqueException {
        when(userRepository.findByEmail(registerEmployeeRequestDto.email())).thenReturn(null);
        User user = userService.registerEmployee(registerEmployeeRequestDto);
        assertNotNull(user);
        assertEquals(registerEmployeeRequestDto.name(), user.getName());
        assertEquals(registerEmployeeRequestDto.email(), user.getEmail());
    }

    @Test
    void registerPatientShouldThrowExceptionWhenEmailExistsInDb() {
        when(userRepository.findByEmail(registerPatientRequestDto.email())).thenReturn(testUser);
        assertThrowsExactly(EmailNotUniqueException.class, () -> userService.registerPatient(registerPatientRequestDto));
    }

    @Test
    void registerPatientShouldReturnPatientObjectWithProperlyPopulatedFields() throws EmailNotUniqueException {
        when(userRepository.findByEmail(registerPatientRequestDto.email())).thenReturn(null);
        Patient patient = userService.registerPatient(registerPatientRequestDto);
        assertNotNull(patient);
        assertEquals(registerPatientRequestDto.name(), patient.getName());
        assertEquals(registerPatientRequestDto.email(), patient.getEmail());
        assertEquals(UserRole.PATIENT.name(), patient.getRole().name());
    }

    @Test
    void loginUserShouldReturnValidAccessTokenAndRefreshToken() {
        when(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginUserRequestDto.email())).thenReturn(testUser);
        when(securityUtils.createJwt(testUser)).thenReturn("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok");
        when(securityUtils.createRefreshToken()).thenReturn("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=");
        AccessTokenDto accessTokenDto = userService.loginUser(loginUserRequestDto);
        assertNotNull(accessTokenDto);
        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok", accessTokenDto.accessToken());
        assertEquals("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=", accessTokenDto.refreshToken());
    }

    @Test
    void refreshAccessTokenShouldThrowExceptionWhenRefreshTokenIsNotFoundInDb() {
        when(userRepository.findByRefreshToken("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=")).thenReturn(null);
        assertThrowsExactly(RefreshTokenNotFoundException.class, () ->
                userService.refreshAccessToken(refreshTokenRequestDto));
    }

    @Test
    void refreshAccessTokenShouldThrowExceptionWhenRefreshTokenIsExpired() {
        testUser.setRefreshTokenExpiryDate(LocalDateTime.now().minusDays(1));
        when(userRepository.findByRefreshToken("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=")).thenReturn(testUser);
        assertThrowsExactly(ExpiredRefreshTokenException.class, () ->
                userService.refreshAccessToken(refreshTokenRequestDto));
    }

    @Test
    void refreshAccessTokenShouldReturnValidAccessTokenAndRefreshToken() {
        testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
        when(userRepository.findByRefreshToken("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=")).thenReturn(testUser);
        when(securityUtils.createJwt(testUser)).thenReturn("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok");
        when(securityUtils.createRefreshToken()).thenReturn("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=");
        AccessTokenDto accessTokenDto = userService.refreshAccessToken(refreshTokenRequestDto);
        assertNotNull(accessTokenDto);
        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok", accessTokenDto.accessToken());
        assertEquals("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=", accessTokenDto.refreshToken());
    }

    @Test
    void refreshAccessTokenShouldThrowExceptionWhenAccessTokenIsInvalid() {
        when(securityUtils.validateJwt(refreshTokenRequestDto.accessToken())).thenThrow(SignatureException.class);
        assertThrowsExactly(InvalidAccessTokenException.class, () ->
                userService.refreshAccessToken(refreshTokenRequestDto));
    }

    @Test
    void refreshAccessTokenShouldNotThrowExceptionWhenAccessTokenIsExpired() {
        when(securityUtils.validateJwt(refreshTokenRequestDto.accessToken())).thenThrow(ExpiredJwtException.class);
        testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
        when(userRepository.findByRefreshToken("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=")).thenReturn(testUser);
        when(securityUtils.createJwt(testUser)).thenReturn("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok");
        when(securityUtils.createRefreshToken()).thenReturn("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=");
        assertDoesNotThrow(() -> userService.refreshAccessToken(refreshTokenRequestDto));
    }
}