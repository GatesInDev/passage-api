package br.com.passage.api.ticket.internal.web;

import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Base dos testes de integração do módulo de passagens.
 *
 * <p>Cada teste nasce com uma passagem já cadastrada e com atalhos para
 * executar requisições como cada perfil de acesso. As alterações são
 * revertidas ao fim de cada teste pela transação.</p>
 */
@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Transactional
abstract class TicketIntegrationTestSupport {

    protected static final String PATH = "/api/v1/tickets";

    @Autowired protected MockMvc mvc;
    @Autowired protected TicketRepository tickets;
    @Autowired protected EntityManager entityManager;

    /** Passagem disponível em todos os testes. */
    protected Ticket existingTicket;

    /** Partida sempre no futuro, exigida pela validação do payload. */
    protected Instant departure;

    @BeforeEach
    void seedTicket() {
        departure = Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

        existingTicket = tickets.save(Ticket.builder()
                .code("PASS-SEED-001")
                .passengerName("Maria Seed")
                .passengerDocument("12345678909")
                .seatId(UUID.randomUUID())
                .price(new BigDecimal("150.00"))
                .departureDateTime(departure)
                .tripId(UUID.randomUUID())
                .isActive(true)
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    protected String createPayload(String code) {
        return """
                {"code":"%s","passengerName":"João da Silva","passengerDocument":"12345678909",
                 "seatId":"%s","price":189.90,"departureDateTime":"%s","tripId":"%s"}
                """.formatted(code, UUID.randomUUID(), departure, UUID.randomUUID());
    }

    protected String updatePayload(String passengerName, boolean isActive) {
        return """
                {"passengerName":"%s","passengerDocument":"12345678909","seatId":"%s",
                 "price":210.50,"departureDateTime":"%s","tripId":"%s","isActive":%s}
                """.formatted(passengerName, UUID.randomUUID(), departure, UUID.randomUUID(), isActive);
    }

    protected RequestPostProcessor principal(UserRole role) {
        User user = new User("Test User", role.name().toLowerCase() + "@example.com", "unused", role, null);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return authentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }

    protected ResultActions as(UserRole role, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).with(principal(role)));
    }

    protected ResultActions admin(MockHttpServletRequestBuilder request) throws Exception {
        return as(UserRole.ADMIN, request);
    }

    protected ResultActions seller(MockHttpServletRequestBuilder request) throws Exception {
        return as(UserRole.TICKET_SELLER, request);
    }

    protected ResultActions inspector(MockHttpServletRequestBuilder request) throws Exception {
        return as(UserRole.INSPECTOR, request);
    }

    protected ResultActions customer(MockHttpServletRequestBuilder request) throws Exception {
        return as(UserRole.CUSTOMER, request);
    }

    protected UUID responseId(ResultActions result) throws Exception {
        return UUID.fromString(
                JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id"));
    }
}
