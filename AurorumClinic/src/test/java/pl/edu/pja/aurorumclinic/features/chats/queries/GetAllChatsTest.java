package pl.edu.pja.aurorumclinic.features.chats.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetAllChats.class})
@ActiveProfiles("test")
class GetAllChatsTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    PatientRepository patientRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    GetAllChats controller;

    @SuppressWarnings("unchecked")
    private Authentication mockAuth(Long userId, String role) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);

        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(role));

        when(auth.getAuthorities()).thenReturn((Collection) authorities);

        return auth;
    }

    @Test
    void shouldReturnChatsForPatientAndUseDoctorRepository() {
        Long patientId = 10L;
        Authentication auth = mockAuth(patientId, "ROLE_PATIENT");

        GetChatsResponse chat1 = new GetChatsResponse(1L, "Jan", "Kowalski", "pic1");
        GetChatsResponse chat2 = new GetChatsResponse(2L, "Anna", "Nowak", "pic2");

        when(doctorRepository.findAllWhoHadConversationWithPatientId(patientId))
                .thenReturn(List.of(chat1, chat2));
        when(objectStorageService.generateUrl("pic1")).thenReturn("https://cdn/pic1");
        when(objectStorageService.generateUrl("pic2")).thenReturn("https://cdn/pic2");

        var resp = controller.getAllMyConversationDtos(auth);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<List<GetChatsResponse>> body = resp.getBody();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).hasSize(2);

        assertThat(body.getData().get(0).getProfilePicture()).isEqualTo("https://cdn/pic1");
        assertThat(body.getData().get(1).getProfilePicture()).isEqualTo("https://cdn/pic2");

        verify(doctorRepository).findAllWhoHadConversationWithPatientId(patientId);
        verifyNoInteractions(patientRepository);
    }

    @Test
    void shouldReturnChatsForDoctorAndUsePatientRepository() {
        Long doctorId = 20L;
        Authentication auth = mockAuth(doctorId, "ROLE_DOCTOR");

        GetChatsResponse chat1 = new GetChatsResponse(3L, "Piotr", "Lewandowski", "keyA");
        GetChatsResponse chat2 = new GetChatsResponse(4L, "Ola", "Zielińska", "keyB");

        when(patientRepository.findAllWhoHadConversationWithDoctorId(doctorId))
                .thenReturn(List.of(chat1, chat2));
        when(objectStorageService.generateUrl("keyA")).thenReturn("signedA");
        when(objectStorageService.generateUrl("keyB")).thenReturn("signedB");

        var resp = controller.getAllMyConversationDtos(auth);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<List<GetChatsResponse>> body = resp.getBody();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).hasSize(2);

        assertThat(body.getData().get(0).getProfilePicture()).isEqualTo("signedA");
        assertThat(body.getData().get(1).getProfilePicture()).isEqualTo("signedB");

        verify(patientRepository).findAllWhoHadConversationWithDoctorId(doctorId);
        verifyNoInteractions(doctorRepository);
    }
}