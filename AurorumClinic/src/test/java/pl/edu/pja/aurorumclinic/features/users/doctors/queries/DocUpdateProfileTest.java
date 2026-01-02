package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.features.users.doctors.DoctorProfileMapper;
import pl.edu.pja.aurorumclinic.features.users.doctors.commands.DocUpdateProfile;
import pl.edu.pja.aurorumclinic.features.users.doctors.commands.MeUpdateProfileRequest;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.DoctorProfileResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {DocUpdateProfile.class})
@ActiveProfiles("test")
class DocUpdateProfileTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    DoctorProfileMapper mapper;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    DocUpdateProfile controller;

    @Test
    void shouldUpdateProfileAndReturnMappedDto() throws IOException {
        Long doctorId = 1L;

        MeUpdateProfileRequest req = new MeUpdateProfileRequest(
                "10 lat doświadczenia",
                "UJ CM",
                "dobry lekarz");

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        DoctorProfileResponse dto = mock(DoctorProfileResponse.class);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(mapper.toResponse(doctor)).thenReturn(dto);

        var resp = controller.updateMyProfile(doctorId, null, req);

        verify(doctorRepository).findById(doctorId);
        verify(mapper).toResponse(doctor);

        assertThat(doctor.getExperience()).isEqualTo("10 lat doświadczenia");
        assertThat(doctor.getEducation()).isEqualTo("UJ CM");
        assertThat(doctor.getDescription()).isEqualTo("dobry lekarz");

        ApiResponse<DoctorProfileResponse> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isSameAs(dto);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingProfileAndDoctorMissing() {
        Long doctorId = 1L;
        MeUpdateProfileRequest req = new MeUpdateProfileRequest(
                "exp", "edu", "desc"
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateMyProfile(doctorId, null, req))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(doctorRepository).findById(doctorId);
        verifyNoInteractions(mapper, objectStorageService);
    }

    @Test
    void shouldUploadProfilePictureAndSetPath() throws IOException {
        Long doctorId = 2L;
        MultipartFile file = mock(MultipartFile.class);
        MeUpdateProfileRequest req = new MeUpdateProfileRequest(
                "exp", "edu", "desc"
        );

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(objectStorageService.uploadObject(file)).thenReturn("images/profile/abc.jpg");

        var resp = controller.updateMyProfile(doctorId, file, req);

        verify(doctorRepository).findById(doctorId);
        verify(objectStorageService).uploadObject(file);

        assertThat(doctor.getProfilePicture()).isEqualTo("images/profile/abc.jpg");

        ApiResponse<?> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo("success");
    }
}
