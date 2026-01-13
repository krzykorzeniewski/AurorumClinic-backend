package pl.edu.pja.aurorumclinic.features.users.users.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UpdateMfa.class})
@ActiveProfiles("test")
class UpdateMfaTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;

    @Autowired
    UpdateMfa controller;

    @Test
    void setUser2faShouldThrowApiNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;
        var request = new UpdateMfa.UpdateUser2FARequest("123456");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.setUser2fa(userId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(tokenService);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void setUser2faShouldValidateTokenAndEnableTwoFactorAuth() {
        Long userId = 1L;
        String otp = "123456";
        var request = new UpdateMfa.UpdateUser2FARequest(otp);

        User userFromDb = User.builder()
                .id(userId)
                .twoFactorAuth(false)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));
        doNothing().when(tokenService).validateAndDeleteToken(any(User.class), anyString());

        assertThatNoException().isThrownBy(() -> controller.setUser2fa(userId, request));

        verify(userRepository).findById(userId);
        verify(tokenService).validateAndDeleteToken(userFromDb, otp);

        assertThat(userFromDb.isTwoFactorAuth()).isTrue();

        verifyNoMoreInteractions(userRepository, tokenService);
    }
}
