package pl.edu.pja.aurorumclinic.features.newsletter;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;

public interface NewsletterMessageRepository extends JpaRepository<NewsletterMessage, Long> {
}
