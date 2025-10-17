package pl.edu.pja.aurorumclinic.features.appointments.chats;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
