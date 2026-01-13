package pl.edu.pja.aurorumclinic.features.users.users.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetMeByIdResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UserGetUserById.class})
@ActiveProfiles("test")
class UserGetUserByIdTest {

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    UserGetUserById controller;

    @Test
    void meGetUserByIdShouldReturnDtoFromRepository() {
        Long userId = 1L;

        GetMeByIdResponse dto = mock(GetMeByIdResponse.class);
        when(userRepository.getMeByIdResponseDto(anyLong())).thenReturn(dto);

        ResponseEntity<ApiResponse<GetMeByIdResponse>> responseEntity =
                controller.meGetUserById(userId);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getData()).isSameAs(dto);

        verify(userRepository).getMeByIdResponseDto(userId);
        verifyNoMoreInteractions(userRepository);
    }
}
