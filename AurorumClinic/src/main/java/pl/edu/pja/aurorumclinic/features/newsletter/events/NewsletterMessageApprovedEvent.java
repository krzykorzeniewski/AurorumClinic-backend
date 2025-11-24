package pl.edu.pja.aurorumclinic.features.newsletter.events;

import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;

public record NewsletterMessageApprovedEvent(NewsletterMessage newsletterMessage) {
}
