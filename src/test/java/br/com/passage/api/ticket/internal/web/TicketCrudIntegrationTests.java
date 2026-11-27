package br.com.passage.api.ticket.internal.web;

import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TicketCrudIntegrationTests extends TicketIntegrationTestSupport {

    @Test
    void sellerRunsTheWholeTicketLifecycle() throws Exception {
        var created = seller(post(PATH).content(createPayload("PASS-100")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("code").value("PASS-100"))
                .andExpect(jsonPath("passengerName").value("João da Silva"))
                .andExpect(jsonPath("isActive").value(true))
                .andExpect(jsonPath("isDeleted").value(false));

        UUID id = responseId(created);

        seller(get(PATH + "/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("code").value("PASS-100"));

        seller(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id)).exists());

        seller(put(PATH + "/{id}", id).content(updatePayload("João da Silva Santos", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("passengerName").value("João da Silva Santos"))
                .andExpect(jsonPath("price").value(210.50));

        admin(delete(PATH + "/{id}", id)).andExpect(status().isNoContent());
    }

    @Test
    void softDeletedTicketDisappearsFromTheApiButRemainsStored() throws Exception {
        UUID id = existingTicket.getId();

        admin(delete(PATH + "/{id}", id)).andExpect(status().isNoContent());

        // Uma passagem cancelada deixa de existir para a API...
        seller(get(PATH + "/{id}", id)).andExpect(status().isNotFound());
        seller(put(PATH + "/{id}", id).content(updatePayload("Tentativa de Alteração", true)))
                .andExpect(status().isNotFound());
        admin(delete(PATH + "/{id}", id)).andExpect(status().isNotFound());
        seller(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id)).doesNotExist());

        // ...mas continua no banco, para fins de auditoria.
        Ticket stored = tickets.findById(id).orElseThrow();
        assertTrue(stored.isDeleted());
        assertFalse(stored.isActive());
        assertEquals("Maria Seed", stored.getPassengerName(), "o PUT não pode alterar passagem cancelada");
    }

    @Test
    void sellerNeverReachesAnotherCompanyTickets() throws Exception {
        UUID foreign = otherCompanyTicket.getId();

        // A listagem traz apenas a própria empresa.
        seller(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(existingTicket.getId())).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(foreign)).doesNotExist());

        // A passagem alheia se comporta como inexistente.
        seller(get(PATH + "/{id}", foreign)).andExpect(status().isNotFound());
        seller(put(PATH + "/{id}", foreign).content(updatePayload("Invasor", true)))
                .andExpect(status().isNotFound());
        admin(delete(PATH + "/{id}", foreign)).andExpect(status().isNoContent());

        assertEquals("Carlos Rival", tickets.findById(foreign).orElseThrow().getPassengerName());
    }

    @Test
    void administratorReachesEveryCompany() throws Exception {
        admin(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(existingTicket.getId())).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(otherCompanyTicket.getId())).exists());

        admin(get(PATH + "/{id}", otherCompanyTicket.getId())).andExpect(status().isOk());
    }

    @Test
    void sellerCompanyWinsOverPayloadAndSeatIsSoldOnlyOnce() throws Exception {
        // Mesmo informando outra empresa, a venda fica na empresa do vendedor.
        String spoofed = createPayload("PASS-SPOOF")
                .replaceFirst("\\{", "{\"companyUuid\":\"" + otherCompany + "\",");

        var created = seller(post(PATH).content(spoofed))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("companyUuid").value(ownCompany.toString()));

        UUID id = responseId(created);
        var sold = tickets.findById(id).orElseThrow();

        // O mesmo assento na mesma viagem não pode ser vendido de novo.
        String sameSeat = """
                {"code":"PASS-OTHER","passengerName":"Outro Passageiro","passengerDocument":"12345678909",
                 "seatId":"%s","price":189.90,"departureDateTime":"%s","tripId":"%s"}
                """.formatted(sold.getSeatId(), departure, sold.getTripId());

        seller(post(PATH).content(sameSeat)).andExpect(status().isConflict());
    }

    @Test
    void unknownIdentifierIsReportedAsNotFound() throws Exception {
        UUID unknown = UUID.randomUUID();

        seller(get(PATH + "/{id}", unknown)).andExpect(status().isNotFound());
        seller(put(PATH + "/{id}", unknown).content(updatePayload("Fulano", true)))
                .andExpect(status().isNotFound());
        admin(delete(PATH + "/{id}", unknown)).andExpect(status().isNotFound());
    }

    @Test
    void duplicatedCodeIsRejectedAsBusinessConflict() throws Exception {
        seller(post(PATH).content(createPayload("PASS-DUP")))
                .andExpect(status().isCreated());

        seller(post(PATH).content(createPayload("PASS-DUP")))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidPayloadIsRejectedWithFieldDetails() throws Exception {
        String invalid = """
                {"code":"","passengerName":"Jo","passengerDocument":"00000000000",
                 "seatId":"%s","price":-50,"departureDateTime":"2020-01-01T08:00:00Z","tripId":"%s"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        seller(post(PATH).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("invalidFields").isArray());
    }

    @Test
    void ticketCodeIsNotEditableAfterCreation() throws Exception {
        UUID id = existingTicket.getId();

        // O payload de atualização não possui o campo "code": mesmo enviado, é ignorado.
        String withCode = updatePayload("Maria Atualizada", true)
                .replaceFirst("\\{", "{\"code\":\"PASS-HACK\",");

        seller(put(PATH + "/{id}", id).content(withCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("PASS-SEED-001"))
                .andExpect(jsonPath("passengerName").value("Maria Atualizada"));
    }

    @Test
    void eachRoleOnlyReachesItsOwnEndpoints() throws Exception {
        UUID id = existingTicket.getId();

        // Fiscal consulta uma passagem, mas não vende, não lista e não cancela.
        inspector(get(PATH + "/{id}", id)).andExpect(status().isOk());
        inspector(get(PATH)).andExpect(status().isForbidden());
        inspector(post(PATH).content(createPayload("PASS-INSP"))).andExpect(status().isForbidden());
        inspector(delete(PATH + "/{id}", id)).andExpect(status().isForbidden());

        // Bilheteiro opera a venda, mas o cancelamento é exclusivo do ADMIN.
        seller(delete(PATH + "/{id}", id)).andExpect(status().isForbidden());

        // Passageiro não acessa o módulo.
        customer(get(PATH)).andExpect(status().isForbidden());
        customer(get(PATH + "/{id}", id)).andExpect(status().isForbidden());
        customer(post(PATH).content(createPayload("PASS-CUST"))).andExpect(status().isForbidden());

        assertFalse(tickets.findById(id).orElseThrow().isDeleted());
    }
}
