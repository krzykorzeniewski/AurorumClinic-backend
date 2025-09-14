//package pl.edu.pja.aurorumclinic.users.services;
//
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.security.SignatureException;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import pl.edu.pja.aurorumclinic.features.auth.dtos.request.LoginUserRequest;
//import pl.edu.pja.aurorumclinic.features.auth.dtos.request.RefreshAccessTokenRequest;
//import pl.edu.pja.aurorumclinic.features.auth.dtos.request.RegisterEmployeeRequest;
//import pl.edu.pja.aurorumclinic.features.auth.dtos.request.RegisterPatientRequest;
//import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
//import pl.edu.pja.aurorumclinic.shared.data.models.User;
//import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
//import pl.edu.pja.aurorumclinic.shared.SecurityUtils;
//import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class UserServiceImplTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private AuthenticationProvider authenticationProvider;
//
//    @Mock
//    private SecurityUtils securityUtils;
//
//    @InjectMocks
//    private UserServiceImpl userService;
//
//    RegisterEmployeeRequest registerEmployeeRequest;
//    RegisterPatientRequest registerPatientRequest;
//    LoginUserRequest loginUserRequest;
//    RefreshAccessTokenRequest refreshAccessTokenRequest;
//    Authentication authentication;
//    User testUser;
//
//    private AutoCloseable closeable;
//
//
//    @BeforeEach
//    void setUp() {
//        closeable = MockitoAnnotations.openMocks(this);
//        registerEmployeeRequest = new RegisterEmployeeRequest("Jan", "Kowalski",
//                "00000000000", LocalDate.now(), "kowalski@pm.me", "abc123", "123123123");
//        registerPatientRequest = new RegisterPatientRequest("Jan", "Kowalski",
//                "00000000000", LocalDate.now(), "kowalski@pm.me", "abc123", "123123123");
//        loginUserRequest = new LoginUserRequest("kowalski@pm.me", "abc123");
//        testUser = new User();
//        authentication = new UsernamePasswordAuthenticationToken(
//                "kowalski@pm.me", null, List.of(new SimpleGrantedAuthority(UserRole.PATIENT.name()))
//        );
//        refreshAccessTokenRequest = new RefreshAccessTokenRequest("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOi" +
//                "JodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
//                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok",
//                "m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=");
//    }
//
//    @AfterEach
//    void tearDown() throws Exception {
//        closeable.close();
//    }
//
//    @Test
//    void registerEmployeeShouldThrowExceptionWhenEmailExistsInDb() {
//        when(userRepository.findByEmail(registerEmployeeRequest.email())).thenReturn(testUser);
//        assertThrowsExactly(EmailNotUniqueException.class, () -> userService.registerEmployee(registerEmployeeRequest));
//    }
//
//    @Test
//    void registerEmployeeShouldReturnUserObjectWithProperlyPopulatedFields() throws EmailNotUniqueException {
//        when(userRepository.findByEmail(registerEmployeeRequest.email())).thenReturn(null);
//        User user = userService.registerEmployee(registerEmployeeRequest);
//        assertNotNull(user);
//        assertEquals(registerEmployeeRequest.name(), user.getName());
//        assertEquals(registerEmployeeRequest.email(), user.getEmail());
//    }
//
//    @Test
//    void registerPatientShouldThrowExceptionWhenEmailExistsInDb() {
//        when(userRepository.findByEmail(registerPatientRequest.email())).thenReturn(testUser);
//        assertThrowsExactly(EmailNotUniqueException.class, () -> userService.registerPatient(registerPatientRequest));
//    }
//
//    @Test
//    void registerPatientShouldReturnPatientObjectWithProperlyPopulatedFields() throws EmailNotUniqueException {
//        when(userRepository.findByEmail(registerPatientRequest.email())).thenReturn(null);
//        Patient patient = userService.registerPatient(registerPatientRequest);
//        assertNotNull(patient);
//        assertEquals(registerPatientRequest.name(), patient.getName());
//        assertEquals(registerPatientRequest.email(), patient.getEmail());
//        assertEquals(UserRole.PATIENT.name(), patient.getRole().name());
//    }
//
////    @Test
////    void loginUserShouldReturnValidAccessTokenAndRefreshToken() {
////        when(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
////                .thenReturn(authentication);
////        when(userRepository.findByEmail(loginUserRequest.email())).thenReturn(testUser);
////        when(securityUtils.createJwt(testUser)).thenReturn("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
////                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
////                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok");
////        when(securityUtils.createRefreshToken()).thenReturn("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=");
////        AccessToken accessToken = userService.loginUser(loginUserRequest);
////        assertNotNull(accessToken);
////        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
////                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
////                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok", accessToken.accessToken());
////        assertEquals("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=", accessToken.refreshToken());
////    }
//
//    @Test
//    void refreshAccessTokenShouldThrowExceptionWhenRefreshTokenIsNotFoundInDb() {
//        when(userRepository.findByRefreshToken("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=")).thenReturn(null);
//        assertThrowsExactly(RefreshTokenNotFoundException.class, () ->
//                userService.refreshAccessToken(refreshAccessTokenRequest));
//    }
//
//    @Test
//    void refreshAccessTokenShouldThrowExceptionWhenRefreshTokenIsExpired() {
//        testUser.setRefreshTokenExpiryDate(LocalDateTime.now().minusDays(1));
//        when(userRepository.findByRefreshToken("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=")).thenReturn(testUser);
//        assertThrowsExactly(ExpiredRefreshTokenException.class, () ->
//                userService.refreshAccessToken(refreshAccessTokenRequest));
//    }
//
////    @Test
////    void refreshAccessTokenShouldReturnValidAccessTokenAndRefreshToken() {
////        testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
////        when(userRepository.findByRefreshToken("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=")).thenReturn(testUser);
////        when(securityUtils.createJwt(testUser)).thenReturn("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
////                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
////                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok");
////        when(securityUtils.createRefreshToken()).thenReturn("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=");
////        AccessToken accessToken = userService.refreshAccessToken(refreshAccessTokenRequest);
////        assertNotNull(accessToken);
////        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
////                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
////                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok", accessToken.accessToken());
////        assertEquals("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=", accessToken.refreshToken());
////    }
//
//    @Test
//    void refreshAccessTokenShouldThrowExceptionWhenAccessTokenIsInvalid() {
//        when(securityUtils.validateJwt(refreshAccessTokenRequest.accessToken())).thenThrow(SignatureException.class);
//        assertThrowsExactly(InvalidAccessTokenException.class, () ->
//                userService.refreshAccessToken(refreshAccessTokenRequest));
//    }
//
//    @Test
//    void refreshAccessTokenShouldNotThrowExceptionWhenAccessTokenIsExpired() {
//        when(securityUtils.validateJwt(refreshAccessTokenRequest.accessToken())).thenThrow(ExpiredJwtException.class);
//        testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
//        when(userRepository.findByRefreshToken("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=")).thenReturn(testUser);
//        when(securityUtils.createJwt(testUser)).thenReturn("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
//                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJtcsOzekBnbWFpbC5jb20iLCJyb2xlIjoiRE9DVE9SIiwiZXhwIjoxNzUzMTg4MTc3fQ." +
//                "X2v1TpferkkGSFFmZ9-HukSpwXbpMUpYvAuP2g-m_Ok");
//        when(securityUtils.createRefreshToken()).thenReturn("m5hYLz2lEzAuU+Guir7SwkD7IfZTF0AzjPXRtfLCkE8=");
//        assertDoesNotThrow(() -> userService.refreshAccessToken(refreshAccessTokenRequest));
//    }
//}