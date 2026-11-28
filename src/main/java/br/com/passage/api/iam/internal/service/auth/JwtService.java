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

/**
 * Serviço responsável por gerar e validar os tokens JWT de acesso.
 *
 * <p>Os tokens são assinados com HMAC-256 e emitidos por {@code passage-api}.</p>
 *
 * <h2>Configuração</h2>
 *
 * <ul>
 *     <li>{@code api.security.token.secret} – chave de assinatura. O valor padrão é
 *         destinado apenas ao ambiente local;</li>
 *     <li>{@code api.security.token.expiration-hours} – validade do token, em horas.
 *         O padrão é 24.</li>
 * </ul>
 */
@Service
public class JwtService {

    @Value("${api.security.token.secret:67Taiane67Taiane67}")
    private String secret;

    @Value("${api.security.token.expiration-hours:24}")
    private Long expirationHours;

    /**
     * Gera o token de acesso do usuário.
     *
     * <p>O e-mail é utilizado como {@code subject}. O perfil, o identificador e a empresa do
     * usuário são incluídos como claims.</p>
     *
     * @param user usuário autenticado
     * @return token JWT assinado
     */
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

    /**
     * Valida a assinatura, o emissor e a expiração do token.
     *
     * @param token token JWT
     * @return e-mail do usuário, ou {@code null} se o token for inválido ou estiver expirado
     */
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

    /**
     * Retorna a validade do token.
     *
     * @return validade em segundos
     */
    public Long getExpirationInSeconds() {
        return this.expirationHours * 3600;
    }

    private Instant generateExpirationDate() {
        return Instant.now().plus(expirationHours, ChronoUnit.HOURS);
    }
}