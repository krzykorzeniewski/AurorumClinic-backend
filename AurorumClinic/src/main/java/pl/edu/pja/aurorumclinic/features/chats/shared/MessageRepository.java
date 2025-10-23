package pl.edu.pja.aurorumclinic.features.chats.shared;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.chats.queries.GetMessageResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {


    @Query("""
            select new pl.edu.pja.aurorumclinic.features.chats.queries.GetMessageResponse(
                        m.sender.id, m.text, m.sentAt
           ) from Message m where
           (m.sender.id = :myId and m.receiver.id = :recipientId) or (m.sender.id = :recipientId and m.receiver.id = :myId)
           """)
    Page<GetMessageResponse> findAllMessagesBetween(Long myId, Long recipientId, Pageable pageable);
}
