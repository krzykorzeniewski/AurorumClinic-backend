package pl.edu.pja.aurorumclinic.features.users.users.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetUserResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetAllUsers.class})
@ActiveProfiles("test")
class GetAllUsersTest {

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    GetAllUsers controller;

    @Test
    void getAllUsersShouldCallFindAllWhenQueryNullAndRoleNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<GetUserResponse> pageFromDb = Page.empty(pageable);

        when(userRepository.findAllUserResponseDtos(pageable, null)).thenReturn(pageFromDb);

        ResponseEntity<ApiResponse<Page<GetUserResponse>>> response =
                controller.getAllUsers(pageable, null, null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(pageFromDb);

        verify(userRepository).findAllUserResponseDtos(pageable, null);
        verify(userRepository, never()).searchAllUserResponseDtos(any(), anyString(), any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllUsersShouldParseRoleCaseInsensitiveAndCallFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<GetUserResponse> pageFromDb = Page.empty(pageable);

        when(userRepository.findAllUserResponseDtos(pageable, UserRole.DOCTOR)).thenReturn(pageFromDb);

        ResponseEntity<ApiResponse<Page<GetUserResponse>>> response =
                controller.getAllUsers(pageable, "doctor", null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(pageFromDb);

        verify(userRepository).findAllUserResponseDtos(pageable, UserRole.DOCTOR);
        verify(userRepository, never()).searchAllUserResponseDtos(any(), anyString(), any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllUsersShouldTreatInvalidRoleAsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<GetUserResponse> pageFromDb = Page.empty(pageable);

        when(userRepository.findAllUserResponseDtos(pageable, null)).thenReturn(pageFromDb);

        ResponseEntity<ApiResponse<Page<GetUserResponse>>> response =
                controller.getAllUsers(pageable, "not-a-role", null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(pageFromDb);

        verify(userRepository).findAllUserResponseDtos(pageable, null);
        verify(userRepository, never()).searchAllUserResponseDtos(any(), anyString(), any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllUsersShouldCallSearchWhenQueryProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        String query = "kow";
        String role = "ADMIN";

        Page<GetUserResponse> pageFromDb = Page.empty(pageable);

        when(userRepository.searchAllUserResponseDtos(pageable, query, UserRole.ADMIN)).thenReturn(pageFromDb);

        ResponseEntity<ApiResponse<Page<GetUserResponse>>> response =
                controller.getAllUsers(pageable, role, query);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(pageFromDb);

        verify(userRepository).searchAllUserResponseDtos(pageable, query, UserRole.ADMIN);
        verify(userRepository, never()).findAllUserResponseDtos(any(), any());
        verifyNoMoreInteractions(userRepository);
    }
}
