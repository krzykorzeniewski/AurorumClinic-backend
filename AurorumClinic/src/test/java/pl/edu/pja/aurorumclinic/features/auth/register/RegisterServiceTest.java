package pl.edu.pja.aurorumclinic.features.auth.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.shared.PasswordValidator;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@SpringBootTest(classes = {RegisterServiceImpl.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class RegisterServiceTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    SpecializationRepository specializationRepository;

    @MockitoBean
    PasswordValidator passwordValidator;

    @Value("${email-verification-token.expiration.minutes}")
    private Integer emailVerificationTokenExpirationInMinutes;

    @Autowired
    RegisterServiceImpl registerService;

}
