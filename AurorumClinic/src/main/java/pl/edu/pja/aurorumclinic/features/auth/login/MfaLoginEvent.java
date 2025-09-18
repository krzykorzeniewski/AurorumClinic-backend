package pl.edu.pja.aurorumclinic.features.auth.login;

import org.springframework.context.ApplicationEvent;

public class MfaLoginEvent extends ApplicationEvent {
    public MfaLoginEvent(Object source) {
        super(source);
    }
}
