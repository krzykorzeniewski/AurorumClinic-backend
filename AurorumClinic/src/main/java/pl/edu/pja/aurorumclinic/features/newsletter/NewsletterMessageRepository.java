package pl.edu.pja.aurorumclinic.features.newsletter;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsletterMessageRepository extends JpaRepository<NewsletterMessage, Long> {

    List<NewsletterMessage> findBySentAtNullAndApprovedTrue();

}
