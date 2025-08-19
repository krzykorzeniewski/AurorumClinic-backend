package pl.edu.pja.aurorumclinic.shared;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.edu.pja.aurorumclinic.features.auth.ApiAuthException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionResolver {


    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleApiException(ApiException ex) {
        return ApiResponse.fail(Map.of(ex.getField(), ex.getMessage()));
    }

    @ExceptionHandler(ApiAuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<?> handleApiAuthException(ApiAuthException ex) {
        return ApiResponse.fail(Map.of(ex.getField(), ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<?> handleBadCredentialsException(BadCredentialsException ex) {
        return ApiResponse.fail(Map.of("credentials", "Invalid email or password"));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleException(Exception ex) {
        return ApiResponse.error("Internal server error");
    }

}
