package pl.edu.pja.aurorumclinic.users.dtos;

import lombok.Builder;

@Builder
public record AccessTokenDto(String accessToken,
                             String refreshToken) {
}
