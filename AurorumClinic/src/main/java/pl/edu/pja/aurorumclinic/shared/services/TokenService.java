package pl.edu.pja.aurorumclinic.shared.services;

import io.jsonwebtoken.io.Encoders;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.shared.data.TokenRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    public String createRandomToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Encoders.BASE64URL.encode(bytes);
    }

    protected String createOtp() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
    }

    @Transactional
    public Token createToken(User user, TokenName tokenName, int minutesValid) {
        String tokenValue = createRandomToken();
        deletePreviousTokens(user.getTokens(), tokenName);
        return tokenRepository.save(Token.builder()
                .rawValue(tokenValue)
                .value(passwordEncoder.encode(tokenValue))
                .expiryDate(LocalDateTime.now().plusMinutes(minutesValid))
                .name(tokenName)
                .user(user)
                .build());
    }

    @Transactional
    public Token createOtpToken(User user, TokenName tokenName, int minutesValid) {
        String otp = createOtp();
        deletePreviousTokens(user.getTokens(), tokenName);
        return tokenRepository.save(Token.builder()
                .rawValue(otp)
                .value(passwordEncoder.encode(otp))
                .expiryDate(LocalDateTime.now().plusMinutes(minutesValid))
                .name(tokenName)
                .user(user)
                .build());
    }

    @Transactional
    public void validateAndDeleteToken(User user, String tokenValue) {
        Token token = user.getTokens().stream()
                .filter(t -> passwordEncoder.matches(tokenValue, t.getValue()))
                .findFirst()
                .orElseThrow(() -> new ApiException("Invalid token", "token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Token is expired", "token");
        }
        tokenRepository.delete(token);
    }

    @Transactional
    protected void deletePreviousTokens(List<Token> userTokens, TokenName tokenName) {
        if (userTokens == null) {
            return;
        }
        List<Token> tokensToDelete = userTokens.stream().filter(
                token -> token.getName().equals(tokenName)
        ).toList();
        tokenRepository.deleteAll(tokensToDelete);
    }

}
