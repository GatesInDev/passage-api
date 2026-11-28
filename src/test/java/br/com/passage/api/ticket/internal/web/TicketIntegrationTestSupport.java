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

    /** Empresa do operador autenticado nos testes. */
    protected final UUID ownCompany = UUID.randomUUID();

    /** Empresa concorrente, usada para verificar o isolamento. */
    protected final UUID otherCompany = UUID.randomUUID();

    /** Passagem da própria empresa, disponível em todos os testes. */
    protected Ticket existingTicket;

    /** Passagem de outra empresa, que o operador não deve enxergar. */
    protected Ticket otherCompanyTicket;

    /** Partida sempre no futuro, exigida pela validação do payload. */
    protected Instant departure;

    @BeforeEach
    void seedTicket() {
        departure = Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

        existingTicket = saveTicket("PASS-SEED-001", "Maria Seed", ownCompany);
        otherCompanyTicket = saveTicket("PASS-SEED-002", "Carlos Rival", otherCompany);

        entityManager.flush();
        entityManager.clear();
    }

    protected Ticket saveTicket(String code, String passengerName, UUID companyUuid) {
        return tickets.save(Ticket.builder()
                .code(code)
                .passengerName(passengerName)
                .passengerDocument("12345678909")
                .seatId(UUID.randomUUID())
                .price(new BigDecimal("150.00"))
                .departureDateTime(departure)
                .tripId(UUID.randomUUID())
                .companyUuid(companyUuid)
                .isActive(true)
                .build());
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
        // O administrador não é vinculado a empresa: enxerga todas.
        UUID companyUuid = role == UserRole.ADMIN ? null : ownCompany;
        return principal(role, companyUuid);
    }

    protected RequestPostProcessor principal(UserRole role, UUID companyUuid) {
        User user = new User("Test User", role.name().toLowerCase() + "@example.com", "unused", role, companyUuid);
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
