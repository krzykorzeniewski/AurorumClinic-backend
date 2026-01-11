package pl.edu.pja.aurorumclinic.features.chats.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.MessageRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GetAllMessagesById.class})
@ActiveProfiles("test")
class GetAllMessagesByIdTest {

    @MockitoBean
    MessageRepository messageRepository;

    @Autowired
    GetAllMessagesById controller;

    @Test
    void shouldReturnMessagesBetweenUsers() {
        Long myId = 1L;
        Long recipientId = 2L;
        Pageable pageable = PageRequest.of(0, 20);

        Page<GetMessageResponse> page = new PageImpl<>(List.of(), pageable, 0);

        when(messageRepository.findAllMessagesBetween(myId, recipientId, pageable))
                .thenReturn(page);

        var resp = controller.getAllMessagesWithUserId(recipientId, myId, pageable);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<Page<GetMessageResponse>> body = resp.getBody();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isSameAs(page);

        verify(messageRepository).findAllMessagesBetween(myId, recipientId, pageable);
    }
}