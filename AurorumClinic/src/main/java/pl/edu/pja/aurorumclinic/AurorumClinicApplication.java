package pl.edu.pja.aurorumclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AurorumClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(AurorumClinicApplication.class, args);
    }

}
