package pl.edu.pja.aurorumclinic.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

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

    @ExceptionHandler(ApiAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<?> handleApiAuthException(ApiAuthenticationException ex) {
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
        ex.printStackTrace();
        return ApiResponse.error("Internal server error");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNoResourceFoundException() {
        return ApiResponse.fail(null);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<?> handleMissingRequestCookieException(MissingRequestCookieException ex) {
        return ApiResponse.fail(Map.of("cookie", ex.getCookieName() + " is missing"));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<?> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return ApiResponse.fail(Map.of("authority", "Access is denied"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String fieldAndMessage = ex.getDetailMessageArguments()[1].toString();
        String field = fieldAndMessage.split(":")[0];
        String message = fieldAndMessage.split(":")[1].trim();
        return ApiResponse.fail(Map.of(field, message));
    }

}
