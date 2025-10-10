package pl.edu.pja.aurorumclinic.shared.exceptions;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionResolver {


    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleApiException(ApiException ex) {
        return ApiResponse.fail(Map.of(ex.getField(), ex.getMessage()));
    }

    @ExceptionHandler(ApiNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleApiNotFoundException(ApiNotFoundException ex) {
        return ApiResponse.fail(Map.of(ex.getField(), ex.getMessage()));
    }

    @ExceptionHandler(ApiConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleApiConflictException(ApiConflictException ex) {
        return ApiResponse.fail(Map.of(ex.getField(), "already in use"));
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

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream().findFirst().orElseThrow().getMessage();
        String field = String.valueOf(ex.getConstraintViolations().stream().findFirst().orElseThrow().getPropertyPath());
        return ApiResponse.fail(Map.of(field, message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
            org.hibernate.exception.ConstraintViolationException causeException =
                    (org.hibernate.exception.ConstraintViolationException) ex.getCause();
            String constraintName = causeException.getConstraintName();
            switch (Objects.requireNonNull(constraintName)) {
                case "uk_user_email" -> {
                    return ApiResponse.fail(Map.of("email", "already in use"));
                }
                case "uk_user_pesel" -> {
                    return ApiResponse.fail(Map.of("pesel", "already in use"));
                }
                case "uk_user_phone_number" -> {
                    return ApiResponse.fail(Map.of("phoneNumber", "already in use"));
                }
                case "uk_doctor_pwz_number" -> {
                    return ApiResponse.fail(Map.of("pwzNumber", "already in use"));
                }
                case "uk_service_name" -> {
                    return ApiResponse.fail(Map.of("name", "already in use"));
                }
                default -> {
                    return ApiResponse.fail(Map.of("duplicate", "already in use"));
                }
            }
        } else {
            return ApiResponse.fail(Map.of("duplicate", "already in use"));
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ApiResponse.fail(Map.of("payload", "invalid message format"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ApiResponse.fail(Map.of(ex.getParameterName(), "parameter is required"));
    }

    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiResponse<?> handleRateLimitException(RateLimitException ex) {
        return ApiResponse.fail(null);
    }

}
