package pl.edu.pja.aurorumclinic.users.dtos;

import lombok.Builder;

@Builder
public record UserIdResponseDto(Long userId,
                                boolean twoFactorAuth) {
}
