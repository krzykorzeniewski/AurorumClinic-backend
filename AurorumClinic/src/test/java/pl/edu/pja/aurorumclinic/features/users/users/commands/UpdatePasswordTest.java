package pl.edu.pja.aurorumclinic.features.users.users.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.PasswordValidator;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UpdatePassword.class})
@ActiveProfiles("test")
class UpdatePasswordTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PasswordValidator passwordValidator;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @Autowired
    UpdatePassword controller;

    @Test
    void updateUserPasswordShouldThrowApiNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;
        var request = new UpdatePassword.UpdatePasswordRequest("NewStrongPassword!1");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateUserPassword(request, userId))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(passwordValidator, passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserPasswordShouldValidateAndEncodeAndSetPassword() {
        Long userId = 1L;
        String raw = "NewStrongPassword!1";
        String encoded = "ENCODED_HASH";
        var request = new UpdatePassword.UpdatePasswordRequest(raw);

        User userFromDb = User.builder()
                .id(userId)
                .password("oldHash")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));
        doNothing().when(passwordValidator).validatePassword(anyString());
        when(passwordEncoder.encode(anyString())).thenReturn(encoded);

        assertThatNoException().isThrownBy(() -> controller.updateUserPassword(request, userId));

        verify(userRepository).findById(userId);
        verify(passwordValidator).validatePassword(raw);
        verify(passwordEncoder).encode(raw);

        assertThat(userFromDb.getPassword()).isEqualTo(encoded);

        verifyNoMoreInteractions(userRepository, passwordValidator, passwordEncoder);
    }
}
