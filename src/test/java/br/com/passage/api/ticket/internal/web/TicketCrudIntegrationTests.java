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
                // O preço continua sendo o da viagem, mesmo numa alteração.
                .andExpect(jsonPath("price").value(TRIP_PRICE.doubleValue()));

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
    void seatIsSoldOnlyOncePerTrip() throws Exception {
        UUID seat = ownSeat.getId();

        seller(post(PATH).content(createPayload("PASS-SEAT-1", ownTrip.getId(), seat)))
                .andExpect(status().isCreated());

        // O mesmo assento na mesma viagem não pode ser vendido de novo.
        seller(post(PATH).content(createPayload("PASS-SEAT-2", ownTrip.getId(), seat)))
                .andExpect(status().isConflict());
    }

    @Test
    void priceAndDepartureComeFromTheTripAndNotFromTheClient() throws Exception {
        // O payload traz preço e horário forjados; ambos devem ser descartados.
        String forged = createPayload("PASS-FORGED")
                .replaceFirst("\\{", "{\"price\":0.01,\"departureDateTime\":\"2030-01-01T00:00:00Z\",");

        var created = seller(post(PATH).content(forged))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("price").value(TRIP_PRICE.doubleValue()))
                .andExpect(jsonPath("companyUuid").value(ownCompany.toString()));

        var sold = tickets.findById(responseId(created)).orElseThrow();
        assertEquals(0, TRIP_PRICE.compareTo(sold.getPrice()), "o preço vem da viagem");
        assertEquals(ownTrip.getDepartureTime(), sold.getDepartureDateTime(), "o horário vem da viagem");
    }

    @Test
    void tripClosedForSaleIsRejected() throws Exception {
        seller(post(PATH).content(createPayload("PASS-DONE", finishedTrip.getId(), UUID.randomUUID())))
                .andExpect(status().isConflict());
    }

    @Test
    void sellerCannotSellTripsOfAnotherCompany() throws Exception {
        seller(post(PATH).content(createPayload("PASS-FOREIGN", otherCompanyTrip.getId(), otherSeat.getId())))
                .andExpect(status().isForbidden());

        // O administrador opera qualquer empresa, e a passagem herda a da viagem.
        admin(post(PATH).content(createPayload("PASS-ADMIN", otherCompanyTrip.getId(), otherSeat.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("companyUuid").value(otherCompany.toString()));
    }

    @Test
    void seatFromAnotherBusIsRejected() throws Exception {
        // O assento existe, mas pertence ao ônibus da outra empresa.
        seller(post(PATH).content(createPayload("PASS-WRONGSEAT", ownTrip.getId(), otherSeat.getId())))
                .andExpect(status().isConflict());

        // Um identificador que não corresponde a assento algum também é recusado.
        seller(post(PATH).content(createPayload("PASS-NOSEAT", ownTrip.getId(), UUID.randomUUID())))
                .andExpect(status().isConflict());
    }

    @Test
    void updateCannotForgePriceNorEscapeTheTripRules() throws Exception {
        UUID id = existingTicket.getId();

        // Preço forjado no payload é descartado: vale o da viagem.
        String forged = updatePayload("Maria Atualizada", true)
                .replaceFirst("\\{", "{\"price\":0.01,\"departureDateTime\":\"2030-01-01T00:00:00Z\",");

        seller(put(PATH + "/{id}", id).content(forged))
                .andExpect(status().isOk())
                .andExpect(jsonPath("price").value(TRIP_PRICE.doubleValue()));

        // Remarcar para viagem encerrada é recusado.
        seller(put(PATH + "/{id}", id)
                .content(updatePayload("Maria", finishedTrip.getId(), ownSeat.getId(), true)))
                .andExpect(status().isConflict());

        // Assento de outro ônibus é recusado também na alteração.
        seller(put(PATH + "/{id}", id)
                .content(updatePayload("Maria", ownTrip.getId(), otherSeat.getId(), true)))
                .andExpect(status().isConflict());

        // Remarcar para viagem de outra empresa é bloqueado.
        seller(put(PATH + "/{id}", id)
                .content(updatePayload("Maria", otherCompanyTrip.getId(), otherSeat.getId(), true)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateKeepsTheSameSeatWithoutClashingWithItself() throws Exception {
        UUID id = existingTicket.getId();

        // Manter o próprio assento não pode ser lido como assento ocupado.
        seller(put(PATH + "/{id}", id)
                .content(updatePayload("Maria Mesma Poltrona", ownTrip.getId(), existingTicket.getSeatId(), true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("seatId").value(existingTicket.getSeatId().toString()));
    }

    @Test
    void unknownTripIsReportedAsNotFound() throws Exception {
        seller(post(PATH).content(createPayload("PASS-NOTRIP", UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isNotFound());
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
                 "seatId":"%s","tripId":"%s"}
                """.formatted(UUID.randomUUID(), ownTrip.getId());

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
