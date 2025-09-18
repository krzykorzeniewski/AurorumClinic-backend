package pl.edu.pja.aurorumclinic.features.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiResponse<?> apiResponse = null;
        if (authException instanceof ApiAuthException) {
            String message = authException.getMessage();
            String field = "accessToken";
            apiResponse = ApiResponse.fail(Map.of(field, message));
        } else {
            apiResponse = ApiResponse.fail(null);
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
