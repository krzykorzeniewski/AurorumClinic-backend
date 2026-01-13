package pl.edu.pja.aurorumclinic.features.users.users.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UpdateUser.class})
@ActiveProfiles("test")
class UpdateUserTest {

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    UpdateUser controller;

    @Test
    void updateUserByIdShouldThrowApiNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;

        UpdateUser.UpdateUserRequest request = UpdateUser.UpdateUserRequest.builder()
                .name("Jan")
                .surname("Kowalski")
                .pesel("12345678901")
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber("123456789")
                .email("jan.kowalski@example.com")
                .twoFactorAuth(true)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateUserById(userId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserByIdShouldUpdateUserAndReturnResponse() {
        Long userId = 1L;

        UpdateUser.UpdateUserRequest request = UpdateUser.UpdateUserRequest.builder()
                .name("Jan")
                .surname("Kowalski")
                .pesel("12345678901")
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber("123456789")
                .email("jan.kowalski@example.com")
                .twoFactorAuth(true)
                .build();

        User userFromDb = User.builder()
                .id(userId)
                .name("StareImie")
                .surname("StareNazwisko")
                .pesel("00000000000")
                .birthdate(LocalDate.of(1980, 1, 1))
                .phoneNumber("999999999")
                .email("old@example.com")
                .twoFactorAuth(false)
                .phoneNumberVerified(true)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userFromDb));

        ResponseEntity<ApiResponse<UpdateUser.UpdateUserResponse>> responseEntity =
                controller.updateUserById(userId, request);

        assertThat(responseEntity.getBody()).isNotNull();
        UpdateUser.UpdateUserResponse response = responseEntity.getBody().getData();
        assertThat(response).isNotNull();

        assertThat(userFromDb.getName()).isEqualTo("Jan");
        assertThat(userFromDb.getSurname()).isEqualTo("Kowalski");
        assertThat(userFromDb.getPesel()).isEqualTo("12345678901");
        assertThat(userFromDb.getBirthdate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(userFromDb.getPhoneNumber()).isEqualTo("123456789");
        assertThat(userFromDb.getEmail()).isEqualTo("jan.kowalski@example.com");
        assertThat(userFromDb.isTwoFactorAuth()).isTrue();

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.name()).isEqualTo("Jan");
        assertThat(response.surname()).isEqualTo("Kowalski");
        assertThat(response.pesel()).isEqualTo("12345678901");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(response.phoneNumber()).isEqualTo("123456789");
        assertThat(response.email()).isEqualTo("jan.kowalski@example.com");
        assertThat(response.twoFactorAuth()).isTrue();
        assertThat(response.phoneNumberVerified()).isTrue();

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }
}
