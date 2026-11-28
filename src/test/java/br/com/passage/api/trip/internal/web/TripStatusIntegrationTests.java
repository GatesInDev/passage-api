package br.com.passage.api.trip.internal.web;

import br.com.passage.api.trip.enums.TripStatus;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.UUID;

import static br.com.passage.api.trip.enums.TripStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TripStatusIntegrationTests extends TripIntegrationTestSupport {

    @Test
    void tripFollowsTheWholeOperationFlow() throws Exception {
        UUID id = ownTrip.getId();

        for (TripStatus next : new TripStatus[] {BOARDING, IN_PROGRESS, COMPLETED}) {
            manager(put(PATH + "/{id}", id).content(updatePayload(next)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("status").value(next.name()));
        }
    }

    @Test
    void keepingTheSameStatusAllowsEditingOtherFields() throws Exception {
        manager(put(PATH + "/{id}", ownTrip.getId())
                .content(updatePayload(ownBus.getId(), ownDriver.getId(), SCHEDULED, "333.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SCHEDULED"))
                .andExpect(jsonPath("price").value(333.00));
    }

    @ParameterizedTest(name = "{0} -> {1} é recusada")
    @CsvSource({
            "SCHEDULED, IN_PROGRESS",
            "SCHEDULED, COMPLETED",
            "BOARDING, SCHEDULED",
            "BOARDING, COMPLETED",
            "IN_PROGRESS, SCHEDULED",
            "IN_PROGRESS, BOARDING"
    })
    void invalidTransitionsAreRejectedAndChangeNothing(TripStatus from, TripStatus to) throws Exception {
        Trip trip = saveTrip("VIAGEM-" + from + "-" + to, ownRoute, ownBus, ownDriver, from);
        entityManager.flush();
        entityManager.clear();

        manager(put(PATH + "/{id}", trip.getId())
                .content(updatePayload(ownBus.getId(), ownDriver.getId(), to, "999.00")))
                .andExpect(status().isConflict());

        Trip stored = trips.findById(trip.getId()).orElseThrow();
        assertEquals(from, stored.getStatus());
        assertEquals(0, new BigDecimal("150.00").compareTo(stored.getPrice()), "o preço não pode mudar quando a transição falha");
    }

    @ParameterizedTest(name = "{0} pode ser cancelada")
    @CsvSource({"SCHEDULED", "BOARDING", "IN_PROGRESS"})
    void tripCanBeCanceledBeforeItEnds(TripStatus from) throws Exception {
        Trip trip = saveTrip("VIAGEM-CANCEL-" + from, ownRoute, ownBus, ownDriver, from);
        entityManager.flush();
        entityManager.clear();

        manager(put(PATH + "/{id}", trip.getId()).content(updatePayload(CANCELED)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("CANCELED"));
    }

    @ParameterizedTest(name = "viagem {0} não pode ser alterada")
    @CsvSource({"COMPLETED", "CANCELED"})
    void finishedTripCannotBeEditedAtAll(TripStatus finalStatus) throws Exception {
        Trip trip = saveTrip("VIAGEM-FINAL-" + finalStatus, ownRoute, ownBus, ownDriver, finalStatus);
        entityManager.flush();
        entityManager.clear();

        // Nem mantendo a mesma situação, nem com ônibus inválido: a trava vem primeiro.
        manager(put(PATH + "/{id}", trip.getId())
                .content(updatePayload(ownBus.getId(), ownDriver.getId(), finalStatus, "1.00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("Não é possível alterar uma viagem que já foi concluída ou cancelada."));
        manager(put(PATH + "/{id}", trip.getId())
                .content(updatePayload(UUID.randomUUID(), ownDriver.getId(), SCHEDULED, "1.00")))
                .andExpect(status().isConflict());

        Trip stored = trips.findById(trip.getId()).orElseThrow();
        assertEquals(finalStatus, stored.getStatus());
        assertEquals(0, new BigDecimal("150.00").compareTo(stored.getPrice()));
    }
}
