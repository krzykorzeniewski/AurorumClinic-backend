package pl.edu.pja.aurorumclinic.features.users.users.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetUserResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetUserById.class})
@ActiveProfiles("test")
class GetUserByIdTest {

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    GetUserById controller;

    @Test
    void getUserByIdShouldThrowApiNotFoundExceptionWhenUserDoesNotExist() {
        Long userId = 1L;

        when(userRepository.findUserResponseDtoById(anyLong())).thenReturn(null);

        assertThatThrownBy(() -> controller.getUserById(userId))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(userRepository).findUserResponseDtoById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserByIdShouldReturnUserResponseWhenExists() {
        Long userId = 1L;

        GetUserResponse dto = mock(GetUserResponse.class);
        when(userRepository.findUserResponseDtoById(anyLong())).thenReturn(dto);

        ResponseEntity<ApiResponse<GetUserResponse>> responseEntity = controller.getUserById(userId);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getData()).isSameAs(dto);

        verify(userRepository).findUserResponseDtoById(userId);
        verifyNoMoreInteractions(userRepository);
    }
}
