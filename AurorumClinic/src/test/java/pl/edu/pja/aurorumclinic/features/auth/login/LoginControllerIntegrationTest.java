package pl.edu.pja.aurorumclinic.features.auth.login;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pl.edu.pja.aurorumclinic.IntegrationTest;
import pl.edu.pja.aurorumclinic.features.auth.login.dtos.LoginUserRequest;
import pl.edu.pja.aurorumclinic.features.auth.login.dtos.TwoFactorAuthLoginRequest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LoginControllerIntegrationTest extends IntegrationTest {

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void loginShouldReturn200AndCookies() throws JsonProcessingException {
        LoginUserRequest request = new LoginUserRequest("piotr.zielinski@example.com", "123");

        MvcTestResult result = mvcTester.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).cookies().containsCookie("Access-Token");
        assertThat(result).cookies().containsCookie("Refresh-Token");
    }

    @Test
    void loginShouldReturn401WhenCredentialsAreInvalid() throws JsonProcessingException {
        LoginUserRequest request = new LoginUserRequest("mariusz@example.com", "12345");

        MvcTestResult result = mvcTester.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(result).cookies().doesNotContainCookies("Access-Token", "Refresh-Token");
    }

    @Test
    void loginShouldReturn200WithoutCookiesWhenUserHasTwoFactorAuthEnabled() throws JsonProcessingException {
        LoginUserRequest request = new LoginUserRequest("tomasz.lewandowski@example.com", "123");

        MvcTestResult result = mvcTester.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).cookies().doesNotContainCookies("Access-Token", "Refresh-Token");
    }

    @Test
    void loginUserWith2faShouldReturn200AndCookies() throws JsonProcessingException {
        TwoFactorAuthLoginRequest request = new TwoFactorAuthLoginRequest("123123", "tomasz.lewandowski@example.com");

        MvcTestResult result = mvcTester.post().uri("/api/auth/login-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).cookies().containsCookie("Access-Token");
        assertThat(result).cookies().containsCookie("Refresh-Token");
    }

    @Test
    void loginUserWith2faShouldReturn4xxWhenCredentialsAreInvalid() throws JsonProcessingException {
        TwoFactorAuthLoginRequest invalidEmailRequest = new TwoFactorAuthLoginRequest("123123", "tomasz.ewandowski@example.com");

        MvcTestResult result = mvcTester.post().uri("/api/auth/login-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest))
                .exchange();

        assertThat(result).hasStatus4xxClientError();
        assertThat(result).cookies().doesNotContainCookies("Access-Token", "Refresh-Token");
    }

}
