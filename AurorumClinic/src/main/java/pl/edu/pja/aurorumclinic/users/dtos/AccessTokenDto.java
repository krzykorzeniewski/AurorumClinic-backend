package pl.edu.pja.aurorumclinic.users.dtos;

import lombok.Builder;

@Builder
public record AccessTokenDto(Long userId,
                             String accessToken,
                             String refreshToken,
                             boolean twoFactorAuth) {
}
