package br.com.passage.api.integration.internal.web;

import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import br.com.passage.api.integration.internal.domain.enums.FiscalDocumentStatus;
import br.com.passage.api.integration.internal.repository.FiscalDocumentRepository;
import br.com.passage.api.integration.internal.service.IssueFiscalDocumentService;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Transactional
class FiscalDocumentIntegrationTests {

    private static final String PATH = "/api/v1/fiscal-documents";

    @Autowired MockMvc mvc;
    @Autowired TicketRepository tickets;
    @Autowired FiscalDocumentRepository documents;
    @Autowired IssueFiscalDocumentService issueService;
    @Autowired EntityManager entityManager;

    private final UUID ownCompany = UUID.randomUUID();
    private final UUID otherCompany = UUID.randomUUID();

    private Ticket ownTicket;
    private Ticket otherTicket;

    @BeforeEach
    void seed() {
        ownTicket = saveTicket("BPE-SEED-001", ownCompany, new BigDecimal("189.90"));
        otherTicket = saveTicket("BPE-SEED-002", otherCompany, new BigDecimal("95.50"));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void issuingProducesAnAuthorizedDocument() {
        FiscalDocument document = issueService.execute(ownTicket.getId());

        assertEquals(FiscalDocumentStatus.AUTHORIZED, document.getStatus());
        assertEquals("63", document.getModel(), "o BP-e é o modelo 63");
        assertEquals(44, document.getAccessKey().length(), "a chave de acesso tem 44 dígitos");
        assertTrue(document.getAccessKey().matches("\\d{44}"), "a chave contém apenas dígitos");
        assertNotNull(document.getProtocol());
        assertNotNull(document.getAuthorizedAt());
        assertEquals(ownTicket.getId(), document.getTicketId());
        assertEquals(new BigDecimal("189.90"), document.getAmount());
    }

    @Test
    void oneTicketProducesASingleDocument() {
        FiscalDocument first = issueService.execute(ownTicket.getId());
        FiscalDocument second = issueService.execute(ownTicket.getId());

        assertEquals(first.getId(), second.getId(), "reprocessar a venda não duplica o documento");
    }

    @Test
    void documentIsReadableByItsOwnCompanyOnly() throws Exception {
        UUID own = issueService.execute(ownTicket.getId()).getId();
        UUID foreign = issueService.execute(otherTicket.getId()).getId();
        entityManager.flush();

        seller(get(PATH + "/{id}", own)).andExpect(status().isOk())
                .andExpect(jsonPath("model").value("63"))
                .andExpect(jsonPath("status").value("AUTHORIZED"));

        seller(get(PATH + "/{id}", foreign)).andExpect(status().isNotFound());

        seller(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(own)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(foreign)).doesNotExist());

        admin(get(PATH + "/{id}", foreign)).andExpect(status().isOk());
    }

    @Test
    void authorizedDocumentCanBeCanceledOnce() throws Exception {
        UUID id = issueService.execute(ownTicket.getId()).getId();
        entityManager.flush();

        admin(post(PATH + "/{id}/cancel", id)).andExpect(status().isOk())
                .andExpect(jsonPath("status").value("CANCELED"))
                .andExpect(jsonPath("canceledAt").exists())
                .andExpect(jsonPath("accessKey").isNotEmpty());

        // Cancelar de novo é conflito: o documento não está mais autorizado.
        admin(post(PATH + "/{id}/cancel", id)).andExpect(status().isConflict());
    }

    @Test
    void retryIsRejectedForAnAuthorizedDocument() throws Exception {
        UUID id = issueService.execute(ownTicket.getId()).getId();
        entityManager.flush();

        seller(post(PATH + "/{id}/retry", id)).andExpect(status().isConflict());
    }

    @Test
    void unknownDocumentIsReportedAsNotFound() throws Exception {
        UUID unknown = UUID.randomUUID();

        seller(get(PATH + "/{id}", unknown)).andExpect(status().isNotFound());
        admin(post(PATH + "/{id}/cancel", unknown)).andExpect(status().isNotFound());
    }

    @Test
    void customerCannotReachFiscalDocuments() throws Exception {
        UUID id = issueService.execute(ownTicket.getId()).getId();
        entityManager.flush();

        as(UserRole.CUSTOMER, get(PATH)).andExpect(status().isForbidden());
        as(UserRole.CUSTOMER, get(PATH + "/{id}", id)).andExpect(status().isForbidden());
        seller(post(PATH + "/{id}/cancel", id)).andExpect(status().isForbidden());
    }

    // ---------- apoio ----------

    private Ticket saveTicket(String code, UUID companyUuid, BigDecimal price) {
        return tickets.save(Ticket.builder()
                .code(code)
                .passengerName("João da Silva")
                .passengerDocument("12345678909")
                .seatId(UUID.randomUUID())
                .price(price)
                .departureDateTime(Instant.now().plus(30, ChronoUnit.DAYS))
                .tripId(UUID.randomUUID())
                .companyUuid(companyUuid)
                .isActive(true)
                .build());
    }

    private RequestPostProcessor principal(UserRole role) {
        UUID companyUuid = role == UserRole.ADMIN ? null : ownCompany;
        User user = new User("Test User", role.name().toLowerCase() + "@example.com",
                "unused", role, companyUuid);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return authentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }

    private ResultActions as(UserRole role, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).with(principal(role)));
    }

    private ResultActions admin(MockHttpServletRequestBuilder request) throws Exception {
        return as(UserRole.ADMIN, request);
    }

    private ResultActions seller(MockHttpServletRequestBuilder request) throws Exception {
        return as(UserRole.TICKET_SELLER, request);
    }
}
