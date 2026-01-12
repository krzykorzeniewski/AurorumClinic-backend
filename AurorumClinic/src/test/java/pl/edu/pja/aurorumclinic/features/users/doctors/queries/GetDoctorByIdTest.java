package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetDoctorById.class})
@ActiveProfiles("test")
class GetDoctorByIdTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    GetDoctorById controller;

    @Test
    void shouldReturnDoctorWhenExists() {
        Long doctorId = 13L;

        Specialization spec = Specialization.builder()
                .id(3L)
                .name("Psychiatra dorosłych")
                .build();

        Opinion op1 = Opinion.builder().rating(5).build();
        Opinion op2 = Opinion.builder().rating(3).build();

        Appointment a1 = mock(Appointment.class);
        Appointment a2 = mock(Appointment.class);
        when(a1.getOpinion()).thenReturn(op1);
        when(a2.getOpinion()).thenReturn(op2);

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .name("Mateusz")
                .surname("Baran")
                .email("mateusz.baran@example.com")
                .education("Uniwersytet Medyczny w Łodzi")
                .experience("Szpital Wojewódzki w Łodzi")
                .description("Psychiatra dorosłych z wieloletnią praktyką")
                .profilePicture("doctors/13.png")
                .specializations(new HashSet<>(List.of(spec)))
                .appointments(List.of(a1, a2))
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(objectStorageService.generateUrl("doctors/13.png")).thenReturn("https://cdn.test/doctors/13.png");

        ResponseEntity<ApiResponse<GetDoctorById.GetDoctorByIdResponse>> res =
                controller.getDoctorById(doctorId);

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).isNotNull();

        GetDoctorById.GetDoctorByIdResponse data = extractData(res.getBody());
        assertThat(data).isNotNull();

        assertThat(data.id()).isEqualTo(doctorId);
        assertThat(data.name()).isEqualTo("Mateusz");
        assertThat(data.surname()).isEqualTo("Baran");
        assertThat(data.email()).isEqualTo("mateusz.baran@example.com");

        assertThat(data.education()).contains("Uniwersytet Medyczny");
        assertThat(data.experience()).contains("Szpital");
        assertThat(data.description()).contains("Psychiatra");

        assertThat(data.profilePicture()).isEqualTo("https://cdn.test/doctors/13.png");

        assertThat(data.specializations()).hasSize(1);
        assertThat(data.specializations().get(0).id()).isEqualTo(3L);
        assertThat(data.specializations().get(0).name()).isEqualTo("Psychiatra dorosłych");

        assertThat(data.rating()).isEqualTo(4);

        verify(doctorRepository).findById(doctorId);
        verify(objectStorageService).generateUrl("doctors/13.png");
        verifyNoMoreInteractions(doctorRepository, objectStorageService);
    }

    @Test
    void shouldReturnRatingZeroWhenNoOpinions() {
        Long doctorId = 14L;

        Specialization spec = Specialization.builder().id(5L).name("Psychoterapeuta").build();

        Appointment a1 = mock(Appointment.class);
        Appointment a2 = mock(Appointment.class);
        when(a1.getOpinion()).thenReturn(null);
        when(a2.getOpinion()).thenReturn(null);

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .name("Karolina")
                .surname("Lis")
                .email("karolina.lis@example.com")
                .education("Uniwersytet SWPS")
                .experience("Centrum Terapii Poznawczej")
                .description("Psychoterapeuta poznawczo-behawioralny")
                .profilePicture(null)
                .specializations(new HashSet<>(List.of(spec)))
                .appointments(List.of(a1, a2))
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(objectStorageService.generateUrl(null)).thenReturn(null);

        ResponseEntity<ApiResponse<GetDoctorById.GetDoctorByIdResponse>> res =
                controller.getDoctorById(doctorId);

        GetDoctorById.GetDoctorByIdResponse data = extractData(res.getBody());
        assertThat(data.rating()).isEqualTo(0);

        verify(doctorRepository).findById(doctorId);
        verify(objectStorageService).generateUrl(null);
        verifyNoMoreInteractions(doctorRepository, objectStorageService);
    }

    @Test
    void shouldThrowNotFoundWhenDoctorMissing() {
        Long doctorId = 999L;

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getDoctorById(doctorId))
                .isInstanceOf(ApiNotFoundException.class);

        verify(doctorRepository).findById(doctorId);
        verifyNoInteractions(objectStorageService);
        verifyNoMoreInteractions(doctorRepository);
    }

    @SuppressWarnings("unchecked")
    private static <T> T extractData(Object apiResponse) {
        if (apiResponse == null) return null;
        try {
            Method m;
            try {
                m = apiResponse.getClass().getMethod("data");
            } catch (NoSuchMethodException ignored) {
                m = apiResponse.getClass().getMethod("getData");
            }
            return (T) m.invoke(apiResponse);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot extract data from ApiResponse. Adjust extractData() to your ApiResponse implementation.",
                    e
            );
        }
    }
}
