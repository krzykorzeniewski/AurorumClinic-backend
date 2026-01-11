package pl.edu.pja.aurorumclinic.shared.data;

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

    @Query("""
           select case
                  when exists (select 1 from Message m where
                                  (m.sender.id = :patientId and m.receiver.id = :doctorId)
                                  or (m.sender.id = :doctorId and m.receiver.id = :patientId))
                  then true
                  else false
           end
           """)
    boolean existsBetweenUsers(Long patientId, Long doctorId);
}
