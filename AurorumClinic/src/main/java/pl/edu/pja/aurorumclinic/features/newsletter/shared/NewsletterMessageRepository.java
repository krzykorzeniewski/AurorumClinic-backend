package pl.edu.pja.aurorumclinic.features.newsletter.shared;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;

import java.util.List;

public interface NewsletterMessageRepository extends JpaRepository<NewsletterMessage, Long> {

    List<NewsletterMessage> getAllByScheduledAtNotNullAndSentAtNull();
    Page<NewsletterMessage> findAllByScheduledAtNotNullAndSentAtNull(Pageable pageable);
}
