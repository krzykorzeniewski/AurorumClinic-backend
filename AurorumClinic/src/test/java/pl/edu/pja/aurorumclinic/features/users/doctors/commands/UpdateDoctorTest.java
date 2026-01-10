package pl.edu.pja.aurorumclinic.features.users.doctors.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UpdateDoctor.class})
@ActiveProfiles("test")
class UpdateDoctorTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    SpecializationRepository specializationRepository;

    @Autowired
    UpdateDoctor controller;

    @Test
    void shouldUpdateDoctorWhenDoctorAndSpecializationsExist() {
        Long doctorId = 10L;

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .specializations(new HashSet<>())
                .build();

        Specialization s1 = Specialization.builder().id(1L).name("Psychoterapia").build();
        Specialization s2 = Specialization.builder().id(2L).name("Psychologia").build();

        Set<Long> specIds = new HashSet<>(Arrays.asList(1L, 2L));

        UpdateDoctor.UpdateDoctorRequest req = new UpdateDoctor.UpdateDoctorRequest(
                "Jan",
                "Kowalski",
                "12345678901",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "jan.kowalski@test.com",
                true,
                true,
                "Opis",
                "Edukacja",
                "Doświadczenie",
                "PWZ123",
                specIds
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(specializationRepository.findAllById(specIds)).thenReturn(List.of(s1, s2));

        ResponseEntity<ApiResponse<UpdateDoctor.UpdateDoctorResponse>> res = controller.updateDoctor(doctorId, req);

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).isNotNull();

        assertThat(doctor.getName()).isEqualTo("Jan");
        assertThat(doctor.getSurname()).isEqualTo("Kowalski");
        assertThat(doctor.getPesel()).isEqualTo("12345678901");
        assertThat(doctor.getBirthdate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(doctor.getPhoneNumber()).isEqualTo("123456789");
        assertThat(doctor.getEmail()).isEqualTo("jan.kowalski@test.com");
        assertThat(doctor.isTwoFactorAuth()).isTrue();
        assertThat(doctor.isPhoneNumberVerified()).isTrue();
        assertThat(doctor.getEducation()).isEqualTo("Edukacja");
        assertThat(doctor.getExperience()).isEqualTo("Doświadczenie");
        assertThat(doctor.getDescription()).isEqualTo("Opis");
        assertThat(doctor.getPwzNumber()).isEqualTo("PWZ123");
        assertThat(doctor.getSpecializations())
                .extracting(Specialization::getId)
                .containsExactlyInAnyOrder(1L, 2L);

        UpdateDoctor.UpdateDoctorResponse bodyData = extractData(res.getBody());
        assertThat(bodyData).isNotNull();
        assertThat(bodyData.id()).isEqualTo(doctorId);
        assertThat(bodyData.name()).isEqualTo("Jan");
        assertThat(bodyData.surname()).isEqualTo("Kowalski");
        assertThat(bodyData.specializations())
                .extracting(UpdateDoctor.UpdateDoctorResponse.SpecializationDto::id)
                .containsExactlyInAnyOrder(1L, 2L);

        verify(doctorRepository).findById(doctorId);
        verify(specializationRepository).findAllById(specIds);
        verifyNoMoreInteractions(doctorRepository, specializationRepository);
    }

    @Test
    void shouldThrowNotFoundWhenDoctorMissing() {
        Long doctorId = 999L;
        Set<Long> specIds = Set.of(1L);

        UpdateDoctor.UpdateDoctorRequest req = new UpdateDoctor.UpdateDoctorRequest(
                "Jan",
                "Kowalski",
                "12345678901",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "jan.kowalski@test.com",
                false,
                false,
                "Opis",
                "Edukacja",
                "Doświadczenie",
                "PWZ123",
                specIds
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateDoctor(doctorId, req))
                .isInstanceOf(ApiNotFoundException.class);

        verify(doctorRepository).findById(doctorId);
        verifyNoInteractions(specializationRepository);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void shouldThrowApiExceptionWhenSomeSpecializationsMissing() {
        Long doctorId = 10L;

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .specializations(new HashSet<>())
                .build();

        Set<Long> specIds = new HashSet<>(Arrays.asList(1L, 2L));
        Specialization onlyOneReturned = Specialization.builder().id(1L).name("Psychoterapia").build();

        UpdateDoctor.UpdateDoctorRequest req = new UpdateDoctor.UpdateDoctorRequest(
                "Jan",
                "Kowalski",
                "12345678901",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "jan.kowalski@test.com",
                false,
                false,
                "Opis",
                "Edukacja",
                "Doświadczenie",
                "PWZ123",
                specIds
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(specializationRepository.findAllById(specIds)).thenReturn(List.of(onlyOneReturned));

        assertThatThrownBy(() -> controller.updateDoctor(doctorId, req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Some specialization ids are not found");

        verify(doctorRepository).findById(doctorId);
        verify(specializationRepository).findAllById(specIds);
        verifyNoMoreInteractions(doctorRepository, specializationRepository);
    }

    @SuppressWarnings("unchecked")
    private static <T> T extractData(Object apiResponse) {
        try {
            Method m;
            try {
                m = apiResponse.getClass().getMethod("data");
            } catch (NoSuchMethodException ignored) {
                m = apiResponse.getClass().getMethod("getData");
            }
            return (T) m.invoke(apiResponse);
        } catch (Exception e) {
            throw new RuntimeException("Cannot extract data from ApiResponse. Adjust extractData() to your ApiResponse implementation.", e);
        }
    }
}
