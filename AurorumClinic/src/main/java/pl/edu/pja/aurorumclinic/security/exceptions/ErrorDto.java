package pl.edu.pja.aurorumclinic.security.exceptions;

import lombok.Builder;

@Builder
public record ErrorDto(Integer status,
                       String error,
                       String message,
                       String path) {
}
