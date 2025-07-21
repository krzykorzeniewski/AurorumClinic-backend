package pl.edu.pja.aurorumclinic.users.dtos;

import lombok.Builder;

@Builder
public record AccessTokenResponseDto(String accessToken,
                                     String refreshToken) {
}
