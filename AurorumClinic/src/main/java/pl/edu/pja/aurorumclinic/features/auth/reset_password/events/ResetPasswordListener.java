package pl.edu.pja.aurorumclinic.features.auth.reset_password.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Component
@RequiredArgsConstructor
public class ResetPasswordListener {

    private final EmailService emailService;
    private final TokenService tokenService;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${mail.frontend.password-reset-link}")
    private String resetPasswordLink;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddres;

    @EventListener
    @Transactional
    public void handleResetPasswordMessageRequestEvent(ResetPasswordMessageRequestedEvent event) {
        User user = event.user();
        Token token = tokenService.createToken(user, TokenName.PASSWORD_RESET, 15);
        String resetLink = resetPasswordLink + token.getRawValue();

        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        String htmlAsText = springTemplateEngine.process("reset-password-email", context);

        emailService.sendEmail(noreplyEmailAddres, user.getEmail(), "Ustaw nowe hasło", htmlAsText);
    }

}
