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
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UpdateEmail.class})
@ActiveProfiles("test")
class UpdateEmailTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;

    @Autowired
    UpdateEmail controller;

    @Test
    void updateUserEmailShouldThrowApiNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;
        var request = new UpdateEmail.UpdateUserEmailRequest("123456");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateUserEmail(userId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(tokenService);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserEmailShouldValidateTokenAndUpdateEmailAndClearPendingEmail() {
        Long userId = 1L;
        String otp = "123456";
        var request = new UpdateEmail.UpdateUserEmailRequest(otp);

        User userFromDb = User.builder()
                .id(userId)
                .email("old@mail.com")
                .pendingEmail("new@mail.com")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));
        doNothing().when(tokenService).validateAndDeleteToken(any(User.class), anyString());

        assertThatNoException().isThrownBy(() -> controller.updateUserEmail(userId, request));

        verify(userRepository).findById(userId);
        verify(tokenService).validateAndDeleteToken(userFromDb, otp);

        assertThat(userFromDb.getEmail()).isEqualTo("new@mail.com");
        assertThat(userFromDb.getPendingEmail()).isNull();

        verifyNoMoreInteractions(userRepository, tokenService);
    }
}
