package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetRecommendedDoctors.class})
@ActiveProfiles("test")
class GetRecommendedDoctorsTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    GetRecommendedDoctors controller;

    @Test
    void getRecommendedDoctorsShouldComputeRatingAndSortDesc() {
        Pageable pageable = PageRequest.of(0, 6);

        Specialization psychiatrist = Specialization.builder().id(10L).name("Psychiatra").build();
        Specialization psychotherapist = Specialization.builder().id(11L).name("Psychoterapeuta").build();

        Opinion op5 = Opinion.builder().rating(5).build();
        Opinion op3 = Opinion.builder().rating(3).build();
        Opinion op1 = Opinion.builder().rating(1).build();

        Appointment a1 = Appointment.builder().opinion(op5).build();
        Appointment a2 = Appointment.builder().opinion(op3).build();
        Appointment aNull = Appointment.builder().opinion(null).build();

        Appointment b1 = Appointment.builder().opinion(op1).build();

        Doctor doctorA = Doctor.builder()
                .id(1L)
                .name("Jan")
                .surname("Nowak")
                .profilePicture("doctors/jan-nowak.jpg")
                .specializations(Set.of(psychiatrist))
                .appointments(List.of(a1, a2, aNull))
                .build();

        Doctor doctorB = Doctor.builder()
                .id(2L)
                .name("Anna")
                .surname("Kowalska")
                .profilePicture("doctors/anna-kowalska.jpg")
                .specializations(Set.of(psychotherapist))
                .appointments(List.of(b1))
                .build();

        Doctor doctorC = Doctor.builder()
                .id(3L)
                .name("Piotr")
                .surname("Zieliński")
                .profilePicture("doctors/piotr-zielinski.jpg")
                .specializations(Set.of(psychiatrist, psychotherapist))
                .appointments(List.of())
                .build();

        when(doctorRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(doctorB, doctorC, doctorA), pageable, 3));

        when(objectStorageService.generateUrl("doctors/jan-nowak.jpg")).thenReturn("url-a");
        when(objectStorageService.generateUrl("doctors/anna-kowalska.jpg")).thenReturn("url-b");
        when(objectStorageService.generateUrl("doctors/piotr-zielinski.jpg")).thenReturn("url-c");

        var responseEntity = controller.getRecommendedDoctors(pageable);
        Object apiResponse = responseEntity.getBody();

        @SuppressWarnings("unchecked")
        List<GetRecommendedDoctors.GetRecommendedDoctorResponse> result =
                (List<GetRecommendedDoctors.GetRecommendedDoctorResponse>) extractApiResponseData(apiResponse);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(GetRecommendedDoctors.GetRecommendedDoctorResponse::id)
                .containsExactly(1L, 2L, 3L);

        assertThat(result.get(0).rating()).isEqualTo(4);
        assertThat(result.get(1).rating()).isEqualTo(1);
        assertThat(result.get(2).rating()).isEqualTo(0);

        assertThat(result.get(0).profilePicture()).isEqualTo("url-a");
        assertThat(result.get(1).profilePicture()).isEqualTo("url-b");
        assertThat(result.get(2).profilePicture()).isEqualTo("url-c");

        verify(doctorRepository).findAll(pageable);
        verify(objectStorageService).generateUrl("doctors/jan-nowak.jpg");
        verify(objectStorageService).generateUrl("doctors/anna-kowalska.jpg");
        verify(objectStorageService).generateUrl("doctors/piotr-zielinski.jpg");
        verifyNoMoreInteractions(doctorRepository, objectStorageService);
    }

    private static Object extractApiResponseData(Object apiResponse) {
        if (apiResponse == null) return null;
        for (String methodName : List.of("data", "getData", "result", "getResult", "payload", "getPayload")) {
            try {
                Method m = apiResponse.getClass().getMethod(methodName);
                return m.invoke(apiResponse);
            } catch (Exception ignored) { }
        }
        fail("Nie udało się wyciągnąć data/result z ApiResponse. Podeślij klasę ApiResponse, dopasuję asercje 1:1.");
        return null;
    }
}
