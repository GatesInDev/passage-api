package br.com.passage.api.integration.internal.web;

import br.com.passage.api.integration.internal.domain.enums.FiscalDocumentStatus;
import br.com.passage.api.integration.internal.repository.FiscalDocumentRepository;
import br.com.passage.api.ticket.TicketSold;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica o caminho assíncrono completo: venda confirmada, evento
 * entregue e documento fiscal emitido.
 *
 * <p>Esta classe não é transacional de propósito. O listener só é
 * acionado depois que a transação da venda é confirmada, de modo que um
 * teste com rollback jamais o exercitaria. Como os dados são realmente
 * gravados, a limpeza é feita ao final de cada teste.</p>
 */
@SpringBootTest(properties = "spring.docker.compose.enabled=false")
class TicketSoldFlowTests {

    @Autowired TicketRepository tickets;
    @Autowired FiscalDocumentRepository documents;
    @Autowired ApplicationEventPublisher events;
    @Autowired TransactionTemplate transactionTemplate;

    private UUID ticketId;

    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            if (ticketId != null) {
                documents.findByTicketId(ticketId).ifPresent(documents::delete);
                tickets.findById(ticketId).ifPresent(tickets::delete);
            }
        });
    }

    @Test
    void sellingATicketEventuallyIssuesTheFiscalDocument() {
        UUID companyUuid = UUID.randomUUID();

        ticketId = transactionTemplate.execute(status -> {
            Ticket ticket = tickets.save(Ticket.builder()
                    .code("BPE-FLOW-" + System.nanoTime())
                    .passengerName("João da Silva")
                    .passengerDocument("12345678909")
                    .seatId(UUID.randomUUID())
                    .price(new BigDecimal("189.90"))
                    .departureDateTime(Instant.now().plus(30, ChronoUnit.DAYS))
                    .tripId(UUID.randomUUID())
                    .companyUuid(companyUuid)
                    .isActive(true)
                    .build());

            // Publicado dentro da transação, entregue somente após o commit.
            events.publishEvent(new TicketSold(ticket.getId(), companyUuid));
            return ticket.getId();
        });

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    var document = documents.findByTicketId(ticketId);
                    assertTrue(document.isPresent(), "o evento deve emitir o documento fiscal");
                    assertEquals(FiscalDocumentStatus.AUTHORIZED, document.get().getStatus());
                    assertNotNull(document.get().getAccessKey());
                    assertEquals(44, document.get().getAccessKey().length());
                });
    }
}
