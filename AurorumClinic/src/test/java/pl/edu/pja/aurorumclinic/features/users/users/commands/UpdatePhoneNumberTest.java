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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UpdatePhoneNumber.class})
@ActiveProfiles("test")
class UpdatePhoneNumberTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TokenService tokenService;

    @Autowired
    UpdatePhoneNumber controller;

    @Test
    void updateUserPhoneNumberShouldThrowApiNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;
        var request = new UpdatePhoneNumber.UpdateUserPhoneNumberRequest("123456");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateUserPhoneNumber(userId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(tokenService);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserPhoneNumberShouldValidateTokenUpdatePhoneNumberAndClearPendingPhoneNumber() {
        Long userId = 1L;
        String otp = "123456";
        var request = new UpdatePhoneNumber.UpdateUserPhoneNumberRequest(otp);

        User userFromDb = User.builder()
                .id(userId)
                .phoneNumber("111111111")
                .pendingPhoneNumber("222222222")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));
        doNothing().when(tokenService).validateAndDeleteToken(any(User.class), anyString());

        assertThatNoException().isThrownBy(() -> controller.updateUserPhoneNumber(userId, request));

        verify(userRepository).findById(userId);
        verify(tokenService).validateAndDeleteToken(userFromDb, otp);

        assertThat(userFromDb.getPhoneNumber()).isEqualTo("222222222");
        assertThat(userFromDb.getPendingPhoneNumber()).isNull();

        verifyNoMoreInteractions(userRepository, tokenService);
    }
}
