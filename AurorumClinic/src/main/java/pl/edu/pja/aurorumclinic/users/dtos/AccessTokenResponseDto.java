package pl.edu.pja.aurorumclinic.users.dtos;

import lombok.Builder;

@Builder
public record AccessTokenResponseDto(Long userId,
                                     String accessToken,
                                     String refreshToken) {
}
