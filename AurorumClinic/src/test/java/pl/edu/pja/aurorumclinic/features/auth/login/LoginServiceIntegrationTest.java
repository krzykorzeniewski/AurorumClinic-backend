package pl.edu.pja.aurorumclinic.features.auth.login;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class LoginServiceIntegrationTest {

    @Autowired
    LoginServiceImpl loginService;

    @Autowired
    UserRepository userRepository;

    @Test
    void Test() {
        System.out.println("test");
    }
}
