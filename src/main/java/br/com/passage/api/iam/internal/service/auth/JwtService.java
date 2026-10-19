package br.com.passage.api.iam.internal.service.auth;

import br.com.passage.api.iam.internal.domain.entities.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    @Value("${api.security.token.secret:67Taiane67Taiane67}")
    private String secret;

    @Value("${api.security.token.expiration-hours:24}")
    private Long expirationHours;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("passage-api")
                    .withSubject(user.getEmail())
                    .withClaim("role", "ROLE_" + user.getRole().name())
                    .withClaim("userId", user.getId().toString())
                    .withClaim("companyUuid", user.getCompanyUuid() != null ? user.getCompanyUuid().toString() : null)
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token de acesso", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("passage-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public Long getExpirationInSeconds() {
        return this.expirationHours * 3600;
    }

    private Instant generateExpirationDate() {
        return Instant.now().plus(expirationHours, ChronoUnit.HOURS);
    }
}