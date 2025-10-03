package pl.edu.pja.aurorumclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AurorumClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(AurorumClinicApplication.class, args);
    }

    /*TODO
    1. końcowka do polecanych lekarzy (pewnie tych z największym ratingiem)
    2. końcówka do wyszukiwania lekarzy z wolnymi terminami (czyli komponent na figmie z kalendarzem,
    z tego komponentu po naciśnięciu na imię i nazwisko lekarza powinno pójść przekierowanie na jego profil)
    3. coś jeszcze wizualnie przemyśleć z headerem i footerem, czy nie dodać tam jakiś rzeczy
     */

}
