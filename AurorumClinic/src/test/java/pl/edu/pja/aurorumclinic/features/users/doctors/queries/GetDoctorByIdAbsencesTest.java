package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetDoctorByIdAbsences.class})
@ActiveProfiles("test")
class GetDoctorByIdAbsencesTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    AbsenceRepository absenceRepository;

    @Autowired
    GetDoctorByIdAbsences controller;

    @Test
    void shouldReturnAbsencesPageWhenDoctorExists() {
        Long doctorId = 4L;
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startedAt").descending());

        when(doctorRepository.findById(doctorId))
                .thenReturn(Optional.of(Doctor.builder().id(doctorId).build()));

        Absence a1 = Absence.builder()
                .id(1L)
                .name("Urlop")
                .startedAt(LocalDateTime.of(2025, 12, 24, 12, 0))
                .finishedAt(LocalDateTime.of(2025, 12, 26, 21, 0))
                .build();

        Absence a2 = Absence.builder()
                .id(2L)
                .name("Konferencja")
                .startedAt(LocalDateTime.of(2025, 11, 10, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 10, 18, 0))
                .build();

        Page<Absence> absencesPage = new PageImpl<>(List.of(a1, a2), pageable, 2);
        when(absenceRepository.findAllByDoctorId(doctorId, pageable)).thenReturn(absencesPage);

        ResponseEntity<ApiResponse<Page<GetDoctorByIdAbsences.GetDoctorAbsenceResponse>>> res =
                controller.getDoctorAbsences(doctorId, pageable);

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).isNotNull();

        Page<GetDoctorByIdAbsences.GetDoctorAbsenceResponse> data = extractData(res.getBody());
        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);

        List<GetDoctorByIdAbsences.GetDoctorAbsenceResponse> content = data.getContent();
        assertThat(content).hasSize(2);

        assertThat(content.get(0).id()).isEqualTo(1L);
        assertThat(content.get(0).name()).isEqualTo("Urlop");
        assertThat(content.get(0).startedAt()).isEqualTo(LocalDateTime.of(2025, 12, 24, 12, 0));
        assertThat(content.get(0).finishedAt()).isEqualTo(LocalDateTime.of(2025, 12, 26, 21, 0));

        assertThat(content.get(1).id()).isEqualTo(2L);
        assertThat(content.get(1).name()).isEqualTo("Konferencja");

        verify(doctorRepository).findById(doctorId);
        verify(absenceRepository).findAllByDoctorId(doctorId, pageable);
        verifyNoMoreInteractions(doctorRepository, absenceRepository);
    }

    @Test
    void shouldThrowNotFoundWhenDoctorMissing() {
        Long doctorId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getDoctorAbsences(doctorId, pageable))
                .isInstanceOf(ApiNotFoundException.class);

        verify(doctorRepository).findById(doctorId);
        verifyNoInteractions(absenceRepository);
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
