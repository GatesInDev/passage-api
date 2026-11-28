package br.com.passage.api.trip.internal.web;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static br.com.passage.api.trip.enums.TripStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Rota inativa e conflito de agenda de ônibus e motorista.
 *
 * <p>A viagem de exemplo da própria empresa ocupa o ônibus e o motorista de
 * {@code departure} até {@code departure + 5h}.</p>
 */
class TripRulesIntegrationTests extends TripIntegrationTestSupport {

    private Instant at(long hours) {
        return departure.plus(hours, ChronoUnit.HOURS);
    }

    private String createAt(String code, UUID busId, UUID driverId, Instant start, Instant end) {
        return createPayload(code, ownRoute.getId(), busId, driverId, start, end);
    }

    @Test
    void inactiveRouteDoesNotReceiveTrips() throws Exception {
        var route = routes.findById(ownRoute.getId()).orElseThrow();
        route.setActive(false);
        routes.saveAndFlush(route);

        manager(post(PATH).content(ownCreatePayload("VIAGEM-ROTA-INATIVA")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("A rota informada está inativa."));
    }

    @Test
    void busCannotBeInTwoTripsAtTheSameTime() throws Exception {
        Driver anotherDriver = saveDriver(ownCompany, "90000000004", true);

        manager(post(PATH).content(createAt("VIAGEM-CONFLITO-ONIBUS", ownBus.getId(), anotherDriver.getId(), at(1), at(3))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("O ônibus informado já possui uma viagem agendada nesse período."));
    }

    @Test
    void driverCannotBeInTwoTripsAtTheSameTime() throws Exception {
        Bus anotherBus = saveBus(ownCompany, "TRP1A04", true);

        manager(post(PATH).content(createAt("VIAGEM-CONFLITO-MOTORISTA", anotherBus.getId(), ownDriver.getId(), at(-2), at(1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("O motorista informado já possui uma viagem agendada nesse período."));
    }

    @Test
    void tripThatCoversTheWholeExistingOneIsAConflict() throws Exception {
        manager(post(PATH).content(createAt("VIAGEM-ENVOLVE", ownBus.getId(), ownDriver.getId(), at(-1), at(6))))
                .andExpect(status().isConflict());
    }

    @Test
    void backToBackTripsAreAllowed() throws Exception {
        // Começa exatamente quando a viagem de exemplo termina.
        manager(post(PATH).content(createAt("VIAGEM-EM-SEGUIDA", ownBus.getId(), ownDriver.getId(), at(5), at(9))))
                .andExpect(status().isCreated());
    }

    @Test
    void canceledOrDeletedTripsFreeTheBusAndDriver() throws Exception {
        Trip canceled = saveTrip("VIAGEM-CANCELADA", ownRoute, ownBus, ownDriver, CANCELED);
        canceled.setDepartureTime(at(24));
        canceled.setArrivalTime(at(29));
        trips.saveAndFlush(canceled);

        manager(post(PATH).content(createAt("VIAGEM-NO-LUGAR-DA-CANCELADA", ownBus.getId(), ownDriver.getId(), at(25), at(28))))
                .andExpect(status().isCreated());

        manager(delete(PATH + "/{id}", ownTrip.getId())).andExpect(status().isNoContent());
        manager(post(PATH).content(createAt("VIAGEM-NO-LUGAR-DA-EXCLUIDA", ownBus.getId(), ownDriver.getId(), at(1), at(4))))
                .andExpect(status().isCreated());
    }

    @Test
    void otherBusAndDriverCanRunAtTheSameTime() throws Exception {
        // O conflito é por ônibus e motorista, não pelo horário da empresa.
        Bus anotherBus = saveBus(ownCompany, "TRP1A05", true);
        Driver anotherDriver = saveDriver(ownCompany, "90000000005", true);

        manager(post(PATH).content(createAt("VIAGEM-OUTROS-RECURSOS", anotherBus.getId(), anotherDriver.getId(), at(1), at(3))))
                .andExpect(status().isCreated());
    }

    @Test
    void updateKeepingItsOwnTimeIsNotAConflict() throws Exception {
        manager(put(PATH + "/{id}", ownTrip.getId())
                .content(updatePayload(ownBus.getId(), ownDriver.getId(), SCHEDULED, "200", departure, at(5))))
                .andExpect(status().isOk());
    }

    @Test
    void updateMovingOntoAnotherTripIsAConflict() throws Exception {
        UUID second = responseId(manager(post(PATH).content(ownCreatePayload("VIAGEM-SEGUNDA")))
                .andExpect(status().isCreated()));

        manager(put(PATH + "/{id}", second)
                .content(updatePayload(ownBus.getId(), ownDriver.getId(), SCHEDULED, "200", at(2), at(4))))
                .andExpect(status().isConflict());

        assertEquals(freeSlot, trips.findById(second).orElseThrow().getDepartureTime());
    }

    @Test
    void tripCanBeCanceledEvenIfItsBusWasDeactivated() throws Exception {
        // Cancelar libera os recursos: não deve depender do ônibus continuar ativo.
        Trip trip = saveTrip("VIAGEM-ONIBUS-DESATIVADO", ownRoute, inactiveBus, ownDriver, SCHEDULED);
        trip.setDepartureTime(at(48));
        trip.setArrivalTime(at(53));
        trips.saveAndFlush(trip);

        manager(put(PATH + "/{id}", trip.getId())
                .content(updatePayload(inactiveBus.getId(), ownDriver.getId(), CANCELED, "150.00", at(48), at(53))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("CANCELED"));
    }
}
