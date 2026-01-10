package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetAllDoctors.class})
@ActiveProfiles("test")
class GetAllDoctorsTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    GetAllDoctors controller;

    @Test
    void shouldReturnPagedDoctorsWithCalculatedRatingAndProfilePictureUrl() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        Specialization spec = Specialization.builder()
                .id(10L)
                .name("Psychoterapia")
                .build();

        Opinion op1 = Opinion.builder().rating(5).build();
        Opinion op2 = Opinion.builder().rating(3).build();

        Appointment ap1 = Appointment.builder().opinion(op1).build();
        Appointment ap2 = Appointment.builder().opinion(op2).build();
        Appointment ap3 = Appointment.builder().opinion(null).build();

        Doctor doctor = Doctor.builder()
                .id(1L)
                .name("Jan")
                .surname("Kowalski")
                .profilePicture("doctors/1.png")
                .specializations(Set.of(spec))
                .appointments(List.of(ap1, ap2, ap3))
                .build();

        Page<Doctor> doctorsPage = new PageImpl<>(List.of(doctor), pageable, 1);

        when(doctorRepository.findAll(pageable)).thenReturn(doctorsPage);
        when(objectStorageService.generateUrl("doctors/1.png")).thenReturn("https://cdn.example/doctors/1.png");

        var responseEntity = controller.getAllDoctors(pageable);

        ApiResponse<Page<GetAllDoctors.GetDoctorResponse>> body = responseEntity.getBody();
        assertThat(body).isNotNull();

        Page<GetAllDoctors.GetDoctorResponse> result = body.getData();

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);

        GetAllDoctors.GetDoctorResponse dto = result.getContent().get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Jan");
        assertThat(dto.surname()).isEqualTo("Kowalski");

        assertThat(dto.profilePicture()).isEqualTo("https://cdn.example/doctors/1.png");
        assertThat(dto.rating()).isEqualTo(4);

        assertThat(dto.specializations()).hasSize(1);
        assertThat(dto.specializations().get(0).id()).isEqualTo(10L);
        assertThat(dto.specializations().get(0).name()).isEqualTo("Psychoterapia");

        verify(doctorRepository).findAll(pageable);
        verify(objectStorageService).generateUrl("doctors/1.png");
        verifyNoMoreInteractions(doctorRepository, objectStorageService);
    }

    @Test
    void shouldReturnRatingZeroWhenNoOpinions() {
        Pageable pageable = PageRequest.of(0, 10);

        Doctor doctor = Doctor.builder()
                .id(2L)
                .name("Anna")
                .surname("Nowak")
                .profilePicture(null)
                .specializations(Set.of())
                .appointments(List.of(
                        Appointment.builder().opinion(null).build(),
                        Appointment.builder().opinion(null).build()
                ))
                .build();

        Page<Doctor> doctorsPage = new PageImpl<>(List.of(doctor), pageable, 1);

        when(doctorRepository.findAll(pageable)).thenReturn(doctorsPage);
        when(objectStorageService.generateUrl(null)).thenReturn(null);

        var responseEntity = controller.getAllDoctors(pageable);

        ApiResponse<Page<GetAllDoctors.GetDoctorResponse>> body = responseEntity.getBody();
        assertThat(body).isNotNull();

        Page<GetAllDoctors.GetDoctorResponse> result = body.getData();

        GetAllDoctors.GetDoctorResponse dto = result.getContent().get(0);
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.rating()).isEqualTo(0);

        verify(doctorRepository).findAll(pageable);
        verify(objectStorageService).generateUrl(null);
        verifyNoMoreInteractions(doctorRepository, objectStorageService);
    }
}
