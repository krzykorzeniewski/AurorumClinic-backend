package pl.edu.pja.aurorumclinic.features.newsletter.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.edu.pja.aurorumclinic.features.newsletter.events.NewsletterMessageApprovedEvent;
import pl.edu.pja.aurorumclinic.features.newsletter.shared.NewsletterMessageRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsletterJobService {

    private final TaskScheduler taskScheduler;
    private final NewsletterEmailJob newsletterEmailJob;
    private final NewsletterMessageRepository newsletterMessageRepository;

    @TransactionalEventListener
    public void onNewsletterMessageApproved(NewsletterMessageApprovedEvent event) {
        taskScheduler.schedule(() -> newsletterEmailJob.execute(event.newsletterMessage().getId()),
                event.newsletterMessage().getScheduledAt()
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
    }

    @EventListener
    public void onApplicationStart(ContextRefreshedEvent contextRefreshedEvent) {
        List<NewsletterMessage> scheduledButNotSentMessages = newsletterMessageRepository
                .getAllByScheduledAtNotNullAndSentAtNull();

        if (!scheduledButNotSentMessages.isEmpty()) {
            for (NewsletterMessage message : scheduledButNotSentMessages) {
            taskScheduler.schedule(() -> newsletterEmailJob.execute(message.getId()),
                    message.getScheduledAt()
                            .atZone(ZoneId.systemDefault())
                            .toInstant());
            }
        }
    }
}
