package pl.edu.pja.aurorumclinic.features.newsletter;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsletterScheduler {

    private final EmailService emailService;
    private final PatientRepository patientRepository;
    private final OpenAiChatModel chatModel;

    @Scheduled(cron = "0 0 19 1 * *")
    public void sendNewsletter() {
        List<Patient> patientsWhoSubscribed = patientRepository.findByNewsletterTrue();
        if (patientsWhoSubscribed.size() > 0) {
            NewsletterEmailMessage newsletterEmailMessage = ChatClient.create(chatModel)
                    .prompt()
                    .user(u -> u.text("Napisz treść e-maila do newslettera kliniki psychiatrycznej Aurorum Clinic, w języku polskim.\n" +
                            "E-mail powinien:\n" +
                            "\n" +
                            "mieć atrakcyjny i zachęcający tytuł (subject line),\n" +
                            "\n" +
                            "zawierać listę kilku praktycznych porad wspierających dobre samopoczucie i kondycję psychiczną,\n" +
                            "\n" +
                            "kończyć się życzliwym przesłaniem i motywacją,\n" +
                            "\n" +
                            "być napisany w stylu profesjonalnym lecz przystępnym dla odbiorcy,\n" +
                            "\n" +
                            "mieć ton ogólny i uniwersalny.\n" +
                            "\n" +
                            "Długość: zwięzła, nie dłuższa niż 200–250 słów." +
                            "\n" +
                            "Uwagi: nie podawaj nazw usług oferowanych przez klinikę, zachęcaj do zapoznania się z ofertą."))
                    .call()
                    .entity(NewsletterEmailMessage.class);
            for (Patient patient: patientsWhoSubscribed) {
                emailService.sendEmail("support@aurorumclinic.pl",
                        patient.getEmail(),
                        newsletterEmailMessage.subject(),
                        newsletterEmailMessage.content());
            }
        }
    }

}
