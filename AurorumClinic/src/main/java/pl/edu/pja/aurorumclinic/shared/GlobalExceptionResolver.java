package pl.edu.pja.aurorumclinic.shared;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pl.edu.pja.aurorumclinic.features.auth.ApiAuthException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionResolver {


    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleApiException(ApiException ex) {
        return ApiResponse.fail(Map.of(ex.getField(), ex.getMessage()));
    }

    @ExceptionHandler(ApiNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleApiNotFoundException(ApiException ex) {
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

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNoResourceFoundException() {
        return ApiResponse.fail(null);
    }

}
