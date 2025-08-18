package pl.edu.pja.aurorumclinic.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconContoller {
    @GetMapping("favicon.ico")
    void returnNoFavicon() {
    }
}
