package br.com.passage.api.trip.internal.web;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.trip.TripApi;
import br.com.passage.api.trip.TripInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static br.com.passage.api.trip.enums.TripStatus.SCHEDULED;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contrato da {@link TripApi}, usada pelo módulo de passagens.
 */
class TripApiIntegrationTests extends TripIntegrationTestSupport {

    @Autowired private TripApi tripApi;

    @Test
    void returnsTheTripContract() {
        TripInfo info = tripApi.findTrip(ownTrip.getId());

        assertEquals(ownTrip.getId(), info.id());
        assertEquals(ownCompany.getId(), info.companyUuid());
        assertEquals(ownBus.getId(), info.busId());
        assertEquals(departure, info.departureTime());
        assertEquals(0, new BigDecimal("150.00").compareTo(info.price()));
        assertEquals(SCHEDULED, info.status());
    }

    @Test
    void worksWithoutAnAuthenticatedUser() {
        // O módulo que chama aplica a própria regra de acesso: a TripApi não filtra por usuário.
        assertEquals(otherCompany.getId(), tripApi.findTrip(otherTrip.getId()).companyUuid());
    }

    @Test
    void deletedOrUnknownTripIsNotFound() {
        var trip = trips.findById(ownTrip.getId()).orElseThrow();
        trip.setDeleted(true);
        trips.saveAndFlush(trip);

        assertThrows(ResourceNotFoundException.class, () -> tripApi.findTrip(ownTrip.getId()));
        assertThrows(ResourceNotFoundException.class, () -> tripApi.findTrip(UUID.randomUUID()));
    }
}
