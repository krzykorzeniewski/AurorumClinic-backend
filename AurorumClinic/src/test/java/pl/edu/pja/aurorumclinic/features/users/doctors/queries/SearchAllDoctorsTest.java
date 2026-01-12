package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {SearchAllDoctors.class})
@ActiveProfiles("test")
class SearchAllDoctorsTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    SearchAllDoctors controller;

    @Test
    void searchAllDoctorsShouldUseFindBySpecializationsServicesIdWhenQueryIsNull() {
        String query = null;
        Long serviceId = 101L;
        Pageable pageable = PageRequest.of(0, 6);

        Specialization psychiatrist = Specialization.builder().id(10L).name("Psychiatra").build();
        Opinion op5 = Opinion.builder().rating(5).build();
        Opinion op3 = Opinion.builder().rating(3).build();

        Appointment a1 = Appointment.builder().opinion(op5).build();
        Appointment a2 = Appointment.builder().opinion(op3).build();
        Appointment aNull = Appointment.builder().opinion(null).build();

        Doctor doctor = Doctor.builder()
                .id(1L)
                .name("Jan")
                .surname("Nowak")
                .profilePicture("doctors/jan-nowak.jpg")
                .specializations(Set.of(psychiatrist))
                .appointments(List.of(a1, a2, aNull))
                .build();

        Page<Doctor> doctorsPage = new PageImpl<>(List.of(doctor), pageable, 1);

        when(doctorRepository.findBySpecializations_Services_Id(serviceId, pageable))
                .thenReturn(doctorsPage);

        when(objectStorageService.generateUrl("doctors/jan-nowak.jpg")).thenReturn("url-jan");

        var responseEntity = controller.searchAllDoctors(query, serviceId, pageable);
        Object apiResponse = responseEntity.getBody();

        @SuppressWarnings("unchecked")
        Page<SearchAllDoctors.GetDoctorResponse> page =
                (Page<SearchAllDoctors.GetDoctorResponse>) extractApiResponseData(apiResponse);

        assertThat(page.getTotalElements()).isEqualTo(1);

        SearchAllDoctors.GetDoctorResponse dto = page.getContent().get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Jan");
        assertThat(dto.surname()).isEqualTo("Nowak");
        assertThat(dto.profilePicture()).isEqualTo("url-jan");
        assertThat(dto.rating()).isEqualTo(4);
        assertThat(dto.specializations())
                .extracting(SearchAllDoctors.GetDoctorResponse.SpecializationDto::name)
                .containsExactly("Psychiatra");

        verify(doctorRepository).findBySpecializations_Services_Id(serviceId, pageable);
        verify(doctorRepository, never()).findAllByQueryAndServiceId(anyString(), any(), anyLong());
        verify(objectStorageService).generateUrl("doctors/jan-nowak.jpg");
        verifyNoMoreInteractions(doctorRepository, objectStorageService);
    }

    @Test
    void searchAllDoctorsShouldUseFindAllByQueryAndServiceIdWhenQueryProvided() {
        String query = "kow";
        Long serviceId = 102L;
        Pageable pageable = PageRequest.of(0, 6);

        Specialization psychotherapist = Specialization.builder().id(11L).name("Psychoterapeuta").build();

        Doctor doctor = Doctor.builder()
                .id(2L)
                .name("Anna")
                .surname("Kowalska")
                .profilePicture("doctors/anna-kowalska.jpg")
                .specializations(Set.of(psychotherapist))
                .appointments(List.of())
                .build();

        Page<Doctor> doctorsPage = new PageImpl<>(List.of(doctor), pageable, 1);

        when(doctorRepository.findAllByQueryAndServiceId(query, pageable, serviceId))
                .thenReturn(doctorsPage);

        when(objectStorageService.generateUrl("doctors/anna-kowalska.jpg")).thenReturn("url-anna");

        var responseEntity = controller.searchAllDoctors(query, serviceId, pageable);
        Object apiResponse = responseEntity.getBody();

        @SuppressWarnings("unchecked")
        Page<SearchAllDoctors.GetDoctorResponse> page =
                (Page<SearchAllDoctors.GetDoctorResponse>) extractApiResponseData(apiResponse);

        assertThat(page.getTotalElements()).isEqualTo(1);

        SearchAllDoctors.GetDoctorResponse dto = page.getContent().get(0);
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.name()).isEqualTo("Anna");
        assertThat(dto.surname()).isEqualTo("Kowalska");
        assertThat(dto.profilePicture()).isEqualTo("url-anna");
        assertThat(dto.rating()).isEqualTo(0);
        assertThat(dto.specializations())
                .extracting(SearchAllDoctors.GetDoctorResponse.SpecializationDto::name)
                .containsExactly("Psychoterapeuta");

        verify(doctorRepository).findAllByQueryAndServiceId(query, pageable, serviceId);
        verify(doctorRepository, never()).findBySpecializations_Services_Id(anyLong(), any());
        verify(objectStorageService).generateUrl("doctors/anna-kowalska.jpg");
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
