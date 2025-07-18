package pl.edu.pja.aurorumclinic.users.dtos;

import lombok.Builder;

@Builder
public record TokenResponseDto(String accessToken,
                               String refreshToken) {
}
