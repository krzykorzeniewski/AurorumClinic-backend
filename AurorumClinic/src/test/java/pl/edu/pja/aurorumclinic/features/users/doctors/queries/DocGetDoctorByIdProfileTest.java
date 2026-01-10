package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.users.doctors.DoctorProfileMapper;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.DoctorProfileResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {DocGetDoctorByIdProfile.class})
@ActiveProfiles("test")
class DocGetDoctorByIdProfileTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    DoctorProfileMapper mapper;

    @Autowired
    DocGetDoctorByIdProfile controller;

    @Test
    void shouldReturnProfileWhenDoctorExists() {
        Long userId = 1L;

        Doctor doctor = mock(Doctor.class);
        DoctorProfileResponse response = mock(DoctorProfileResponse.class);

        when(doctorRepository.findById(userId)).thenReturn(Optional.of(doctor));
        when(mapper.toResponse(doctor)).thenReturn(response);

        controller.getMyProfile(userId);

        verify(doctorRepository).findById(userId);
        verify(mapper).toResponse(doctor);
        verifyNoMoreInteractions(doctorRepository, mapper);
    }

    @Test
    void shouldThrowNotFoundWhenDoctorMissing() {
        Long userId = 999L;

        when(doctorRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getMyProfile(userId))
                .isInstanceOf(ApiNotFoundException.class);

        verify(doctorRepository).findById(userId);
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(doctorRepository);
    }
}
