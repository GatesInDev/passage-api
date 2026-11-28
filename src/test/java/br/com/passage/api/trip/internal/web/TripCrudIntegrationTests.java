package br.com.passage.api.trip.internal.web;

import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static br.com.passage.api.trip.enums.TripStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TripCrudIntegrationTests extends TripIntegrationTestSupport {

    @Test
    void managerRunsTheWholeTripLifecycle() throws Exception {
        var created = manager(post(PATH).content(ownCreatePayload("VIAGEM-100")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("code").value("VIAGEM-100"))
                .andExpect(jsonPath("companyUuid").value(ownCompany.getId().toString()))
                .andExpect(jsonPath("status").value("SCHEDULED"))
                .andExpect(jsonPath("isActive").value(true));

        UUID id = responseId(created);

        manager(get(PATH + "/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("code").value("VIAGEM-100"));

        manager(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id)).exists());

        manager(put(PATH + "/{id}", id).content(updatePayload(BOARDING)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("BOARDING"))
                .andExpect(jsonPath("price").value(210.00))
                .andExpect(jsonPath("code").value("VIAGEM-100"))
                .andExpect(jsonPath("routeId").value(ownRoute.getId().toString()));

        manager(delete(PATH + "/{id}", id)).andExpect(status().isNoContent());
    }

    @Test
    void tripInheritsTheCompanyOfTheRoute() throws Exception {
        String payload = createPayload("VIAGEM-ADM", otherRoute.getId(), otherBus.getId(), otherDriver.getId());

        admin(post(PATH).content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("companyUuid").value(otherCompany.getId().toString()));
    }

    @Test
    void managerCannotScheduleOnAnotherCompanyRoute() throws Exception {
        String payload = createPayload("VIAGEM-ALHEIA", otherRoute.getId(), otherBus.getId(), otherDriver.getId());

        manager(post(PATH).content(payload)).andExpect(status().isForbidden());
    }

    @Test
    void unknownRouteIsReportedAsNotFound() throws Exception {
        String payload = createPayload("VIAGEM-SEM-ROTA", UUID.randomUUID(), ownBus.getId(), ownDriver.getId());

        admin(post(PATH).content(payload)).andExpect(status().isNotFound());
    }

    @Test
    void busAndDriverMustExistAndBelongToTheRouteCompany() throws Exception {
        UUID route = ownRoute.getId();

        manager(post(PATH).content(createPayload("V-1", route, UUID.randomUUID(), ownDriver.getId())))
                .andExpect(status().isNotFound());
        manager(post(PATH).content(createPayload("V-2", route, ownBus.getId(), UUID.randomUUID())))
                .andExpect(status().isNotFound());
        manager(post(PATH).content(createPayload("V-3", route, otherBus.getId(), ownDriver.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("O ônibus informado pertence a outra empresa."));
        manager(post(PATH).content(createPayload("V-4", route, ownBus.getId(), otherDriver.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("O motorista informado pertence a outra empresa."));
        manager(post(PATH).content(createPayload("V-5", route, inactiveBus.getId(), ownDriver.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("O ônibus informado está inativo."));
        manager(post(PATH).content(createPayload("V-6", route, ownBus.getId(), inactiveDriver.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("O motorista informado está inativo."));
    }

    @Test
    void updateValidatesBusAndDriverToo() throws Exception {
        UUID id = ownTrip.getId();

        manager(put(PATH + "/{id}", id).content(updatePayload(otherBus.getId(), ownDriver.getId(), SCHEDULED, "200")))
                .andExpect(status().isConflict());
        manager(put(PATH + "/{id}", id).content(updatePayload(ownBus.getId(), inactiveDriver.getId(), SCHEDULED, "200")))
                .andExpect(status().isConflict());

        assertEquals(ownBus.getId(), trips.findById(id).orElseThrow().getBusId());
    }

    @Test
    void arrivalMustBeAfterDeparture() throws Exception {
        Instant before = departure.minus(1, ChronoUnit.HOURS);

        manager(post(PATH).content(createPayload("V-DATA", ownRoute.getId(), ownBus.getId(), ownDriver.getId(),
                departure, before))).andExpect(status().isConflict());

        manager(put(PATH + "/{id}", ownTrip.getId()).content(
                updatePayload(ownBus.getId(), ownDriver.getId(), SCHEDULED, "200", departure, before)))
                .andExpect(status().isConflict());
    }

    @Test
    void departureInThePastIsRejectedOnCreation() throws Exception {
        Instant past = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

        manager(post(PATH).content(createPayload("V-PASSADO", ownRoute.getId(), ownBus.getId(), ownDriver.getId(),
                past, past.plus(5, ChronoUnit.HOURS))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("invalidFields").isArray());
    }

    @Test
    void duplicatedCodeIsRejectedAsBusinessConflict() throws Exception {
        manager(post(PATH).content(ownCreatePayload("VIAGEM-DUP"))).andExpect(status().isCreated());
        manager(post(PATH).content(ownCreatePayload("VIAGEM-DUP"))).andExpect(status().isConflict());
    }

    @Test
    void managerNeverReachesAnotherCompanyTrips() throws Exception {
        UUID foreign = otherTrip.getId();

        manager(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(ownTrip.getId())).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(foreign)).doesNotExist());

        manager(get(PATH + "/{id}", foreign)).andExpect(status().isNotFound());
        manager(put(PATH + "/{id}", foreign).content(updatePayload(otherBus.getId(), otherDriver.getId(), BOARDING, "1")))
                .andExpect(status().isNotFound());
        manager(delete(PATH + "/{id}", foreign)).andExpect(status().isNotFound());

        Trip stored = trips.findById(foreign).orElseThrow();
        assertEquals(SCHEDULED, stored.getStatus());
        assertFalse(stored.isDeleted());
    }

    @Test
    void managerWithoutCompanyIsDenied() throws Exception {
        as(UserRole.FLEET_MANAGER, null, get(PATH)).andExpect(status().isForbidden());
        as(UserRole.FLEET_MANAGER, null, get(PATH + "/{id}", ownTrip.getId())).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"ADMIN", "CUSTOMER", "TICKET_SELLER", "INSPECTOR"})
    void otherProfilesSeeTheTripsOfEveryCompany(UserRole role) throws Exception {
        as(role, get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(ownTrip.getId())).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(otherTrip.getId())).exists());

        as(role, get(PATH + "/{id}", otherTrip.getId())).andExpect(status().isOk());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"CUSTOMER", "TICKET_SELLER", "INSPECTOR"})
    void onlyAdministratorsAndManagersChangeTrips(UserRole role) throws Exception {
        UUID id = ownTrip.getId();

        as(role, post(PATH).content(ownCreatePayload("V-" + role))).andExpect(status().isForbidden());
        as(role, put(PATH + "/{id}", id).content(updatePayload(BOARDING))).andExpect(status().isForbidden());
        as(role, delete(PATH + "/{id}", id)).andExpect(status().isForbidden());

        assertEquals(SCHEDULED, trips.findById(id).orElseThrow().getStatus());
    }

    @Test
    void softDeletedTripDisappearsFromTheApiButRemainsStored() throws Exception {
        UUID id = ownTrip.getId();

        manager(delete(PATH + "/{id}", id)).andExpect(status().isNoContent());

        admin(get(PATH + "/{id}", id)).andExpect(status().isNotFound());
        manager(put(PATH + "/{id}", id).content(updatePayload(BOARDING))).andExpect(status().isNotFound());
        manager(delete(PATH + "/{id}", id)).andExpect(status().isNotFound());
        admin(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id)).doesNotExist());

        Trip stored = trips.findById(id).orElseThrow();
        assertTrue(stored.isDeleted());
        assertFalse(stored.isActive());
        assertEquals(SCHEDULED, stored.getStatus(), "o PUT não pode alterar viagem excluída");
    }

    @Test
    void unknownIdentifierIsReportedAsNotFound() throws Exception {
        UUID unknown = UUID.randomUUID();

        admin(get(PATH + "/{id}", unknown)).andExpect(status().isNotFound());
        admin(put(PATH + "/{id}", unknown).content(updatePayload(BOARDING))).andExpect(status().isNotFound());
        admin(delete(PATH + "/{id}", unknown)).andExpect(status().isNotFound());
    }

    @Test
    void tripCodeAndRouteAreNotEditable() throws Exception {
        UUID id = ownTrip.getId();

        // O payload de atualização não possui "code" nem "routeId": mesmo enviados, são ignorados.
        String withCodeAndRoute = updatePayload(BOARDING)
                .replaceFirst("\\{", "{\"code\":\"VIAGEM-HACK\",\"routeId\":\"" + otherRoute.getId() + "\",");

        manager(put(PATH + "/{id}", id).content(withCodeAndRoute))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("VIAGEM-SEED-001"))
                .andExpect(jsonPath("routeId").value(ownRoute.getId().toString()))
                .andExpect(jsonPath("companyUuid").value(ownCompany.getId().toString()));
    }
}
