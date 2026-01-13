package pl.edu.pja.aurorumclinic.features.users.users.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {DisableMfa.class})
@ActiveProfiles("test")
class DisableMfaTest {

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    DisableMfa controller;

    @Test
    void disableMfaShouldThrowApiNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.disableMfa(userId))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void disableMfaShouldThrowApiExceptionWhenUserHasMfaDisabled() {
        Long userId = 1L;

        User userFromDb = User.builder()
                .id(userId)
                .twoFactorAuth(false)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));

        assertThatThrownBy(() -> controller.disableMfa(userId))
                .isExactlyInstanceOf(ApiException.class)
                .hasMessageContaining("mfa disabled");

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void disableMfaShouldSetTwoFactorAuthFalseWhenEnabled() {
        Long userId = 1L;

        User userFromDb = User.builder()
                .id(userId)
                .twoFactorAuth(true)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));

        assertThatNoException().isThrownBy(() -> controller.disableMfa(userId));

        assertThat(userFromDb.isTwoFactorAuth()).isFalse();

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }
}
