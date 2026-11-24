package br.com.passage.api.fleet.internal.web;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SeatCrudIntegrationTests extends FleetIntegrationTestSupport {

    private static final String CREATE = """
            {"seatNumber":"02","row":1,"column":2,"position":"AISLE","quality":"COMFORT","isAccessible":true}
            """;
    private static final String UPDATE = """
            {"seatNumber":"03","row":2,"column":1,"position":"WINDOW","quality":"PREMIUM",
             "isAccessible":false,"isActive":true}
            """;

    @Test
    void managerCannotAccessAnySeatOperationOnAnotherTenantBus() throws Exception {
        manager(get(seatPath(otherBus))).andExpect(status().isNotFound());
        manager(get(seatPath(otherBus) + "/" + otherSeat.getId())).andExpect(status().isNotFound());
        manager(post(seatPath(otherBus)).content(CREATE)).andExpect(status().isNotFound());
        manager(put(seatPath(otherBus) + "/" + otherSeat.getId()).content(UPDATE)).andExpect(status().isNotFound());
        manager(delete(seatPath(otherBus) + "/" + otherSeat.getId())).andExpect(status().isNotFound());
        assertEquals(2, seats.count());
    }

    @Test
    void seatIdCannotBeSubstitutedUnderAnAccessibleBus() throws Exception {
        manager(get(seatPath(ownBus) + "/" + otherSeat.getId())).andExpect(status().isNotFound());
        manager(put(seatPath(ownBus) + "/" + otherSeat.getId()).content(UPDATE)).andExpect(status().isNotFound());
        manager(delete(seatPath(ownBus) + "/" + otherSeat.getId())).andExpect(status().isNotFound());
        admin(get(seatPath(ownBus) + "/" + otherSeat.getId())).andExpect(status().isNotFound());
    }

    @Test
    void ownSeatCrudUpdatesCapacityAndKeepsDeletedSeatsHidden() throws Exception {
        UUID id = responseId(manager(post(seatPath(ownBus)).content(CREATE)).andExpect(status().isCreated())
                .andExpect(jsonPath("busId").value(ownBus.getId().toString()))
                .andExpect(jsonPath("isActive").value(true)));
        manager(get(seatPath(ownBus))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        manager(get("/api/v1/buses/{id}", ownBus.getId())).andExpect(jsonPath("capacity").value(2));
        manager(put(seatPath(ownBus) + "/" + id).content(UPDATE)).andExpect(status().isOk())
                .andExpect(jsonPath("seatNumber").value("03"));
        manager(delete(seatPath(ownBus) + "/" + id)).andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();
        var deleted = seats.findById(id).orElseThrow();
        assertTrue(deleted.isDeleted());
        assertFalse(deleted.isActive());
        manager(get(seatPath(ownBus))).andExpect(jsonPath("$.length()").value(1));
        manager(get("/api/v1/buses/{id}", ownBus.getId())).andExpect(jsonPath("capacity").value(1));
        admin(get(seatPath(ownBus) + "/" + id)).andExpect(status().isNotFound());
        manager(put(seatPath(ownBus) + "/" + id).content(UPDATE)).andExpect(status().isNotFound());
        manager(delete(seatPath(ownBus) + "/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void administratorCanManageSeatsAcrossTenants() throws Exception {
        admin(get(seatPath(otherBus))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        UUID id = responseId(admin(post(seatPath(otherBus)).content(CREATE)).andExpect(status().isCreated()));
        admin(put(seatPath(otherBus) + "/" + id).content(UPDATE)).andExpect(status().isOk());
        admin(delete(seatPath(otherBus) + "/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void inactiveAndDeletedSeatsDoNotCountTowardCapacity() throws Exception {
        manager(put(seatPath(ownBus) + "/" + ownSeat.getId()).content(UPDATE.replace("\"isActive\":true", "\"isActive\":false")))
                .andExpect(status().isOk());
        manager(get("/api/v1/buses/{id}", ownBus.getId())).andExpect(jsonPath("capacity").value(0));
        var deleted = seats.findById(ownSeat.getId()).orElseThrow();
        deleted.setDeleted(true);
        deleted.setActive(true);
        entityManager.flush();
        entityManager.clear();
        manager(get("/api/v1/buses/{id}", ownBus.getId())).andExpect(jsonPath("capacity").value(0));
    }

    @Test
    void deletedBusHidesSeatsAndPreventsNewSeats() throws Exception {
        manager(delete("/api/v1/buses/{id}", ownBus.getId())).andExpect(status().isNoContent());
        admin(get(seatPath(ownBus))).andExpect(status().isNotFound());
        admin(get(seatPath(ownBus) + "/" + ownSeat.getId())).andExpect(status().isNotFound());
        manager(post(seatPath(ownBus)).content(CREATE)).andExpect(status().isNotFound());
        manager(put(seatPath(ownBus) + "/" + ownSeat.getId()).content(UPDATE)).andExpect(status().isNotFound());
        manager(delete(seatPath(ownBus) + "/" + ownSeat.getId())).andExpect(status().isNotFound());
        assertTrue(seats.existsById(ownSeat.getId()));
    }

    @Test
    void numberAndPositionMustBeUniqueWithinBusIncludingDeletedSeats() throws Exception {
        manager(post(seatPath(ownBus)).content(CREATE.replace("\"02\"", "\"01\""))).andExpect(status().isConflict());
        manager(post(seatPath(ownBus)).content(CREATE.replace("\"column\":2", "\"column\":1"))).andExpect(status().isConflict());
        UUID id = responseId(manager(post(seatPath(ownBus)).content(CREATE)).andExpect(status().isCreated()));
        manager(put(seatPath(ownBus) + "/" + id).content(UPDATE.replace("\"03\"", "\"01\"")))
                .andExpect(status().isConflict());
        manager(put(seatPath(ownBus) + "/" + id).content(CREATE.replace("}", ",\"isActive\":true}")))
                .andExpect(status().isOk());
        manager(delete(seatPath(ownBus) + "/" + id)).andExpect(status().isNoContent());
        manager(post(seatPath(ownBus)).content(CREATE)).andExpect(status().isConflict());
        admin(post(seatPath(otherBus)).content(CREATE)).andExpect(status().isCreated());
    }

    @Test
    void invalidSeatLayoutIsRejected() throws Exception {
        manager(post(seatPath(ownBus)).content(CREATE.replace("\"row\":1", "\"row\":0"))).andExpect(status().isBadRequest());
        manager(post(seatPath(ownBus)).content(CREATE.replace("\"column\":2", "\"column\":-1"))).andExpect(status().isBadRequest());
        manager(post(seatPath(ownBus)).content("{}")).andExpect(status().isBadRequest());
        manager(put(seatPath(ownBus) + "/" + ownSeat.getId()).content(CREATE)).andExpect(status().isBadRequest());
        assertEquals(2, seats.count());
    }
}
