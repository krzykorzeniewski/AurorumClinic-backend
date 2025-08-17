package pl.edu.pja.aurorumclinic.users.dtos;

import lombok.Builder;

@Builder
public record AccessToken(Long userId,
                          String accessToken,
                          String refreshToken,
                          boolean twoFactorAuth) {
}
