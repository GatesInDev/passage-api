package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.iam.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CompanyCrudIntegrationTests extends FleetIntegrationTestSupport {

    private static final String PAYLOAD = """
            {"corporateName":"Updated Company","tradeName":"Updated Trading",
             "documentId":"12345678000195","stateRegistration":"ISENTO","stateRegulatoryAgency":"ANTT"}
            """;

    @Test
    void managerListsAndReadsOnlyOwnCompany() throws Exception {
        manager(get("/api/v1/companies"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ownCompany.getId().toString()));
        manager(get("/api/v1/companies/{id}", ownCompany.getId())).andExpect(status().isOk());
        manager(get("/api/v1/companies/{id}", otherCompany.getId())).andExpect(status().isForbidden());
        manager(patch("/api/v1/companies/{id}", otherCompany.getId()).content(PAYLOAD))
                .andExpect(status().isForbidden());
        assertEquals("Second Company", companies.findById(otherCompany.getId()).orElseThrow().getCorporateName());
    }

    @Test
    void administratorCanCreateAndListBothTenants() throws Exception {
        admin(get("/api/v1/companies")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        admin(post("/api/v1/companies").content(PAYLOAD)).andExpect(status().isCreated())
                .andExpect(jsonPath("isActive").value(true));
        admin(post("/api/v1/companies").content(PAYLOAD)).andExpect(status().isConflict());
    }

    @Test
    void managerCanUpdateOwnCompanyButCannotCreateOrDeleteTenants() throws Exception {
        manager(patch("/api/v1/companies/{id}", ownCompany.getId()).content(PAYLOAD))
                .andExpect(status().isOk()).andExpect(jsonPath("corporateName").value("Updated Company"));
        manager(post("/api/v1/companies").content(PAYLOAD)).andExpect(status().isForbidden());
        manager(delete("/api/v1/companies/{id}", ownCompany.getId())).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/companies").with(principal(UserRole.FLEET_MANAGER, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletedCompanyAndItsFleetAreHiddenFromManagersAndAdministrators() throws Exception {
        admin(delete("/api/v1/companies/{id}", ownCompany.getId())).andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();
        var deleted = companies.findById(ownCompany.getId()).orElseThrow();
        assertTrue(deleted.isDeleted());
        assertFalse(deleted.isActive());
        manager(get("/api/v1/companies")).andExpect(jsonPath("$.length()").value(0));
        manager(get("/api/v1/buses")).andExpect(jsonPath("$.length()").value(0));
        manager(get("/api/v1/drivers")).andExpect(jsonPath("$.length()").value(0));
        admin(get("/api/v1/companies")).andExpect(jsonPath("$.length()").value(1));
        admin(get("/api/v1/buses")).andExpect(jsonPath("$.length()").value(1));
        admin(get("/api/v1/drivers")).andExpect(jsonPath("$.length()").value(1));
        admin(get("/api/v1/companies/{id}", ownCompany.getId())).andExpect(status().isNotFound());
        manager(patch("/api/v1/companies/{id}", ownCompany.getId()).content(PAYLOAD)).andExpect(status().isNotFound());
        admin(get("/api/v1/buses/{id}", ownBus.getId())).andExpect(status().isNotFound());
        admin(get("/api/v1/drivers/{id}", ownDriver.getId())).andExpect(status().isNotFound());
        admin(get(seatPath(ownBus))).andExpect(status().isNotFound());
        assertTrue(buses.existsById(ownBus.getId()));
        assertTrue(drivers.existsById(ownDriver.getId()));
    }

    @Test
    void invalidCompanyPayloadIsRejectedBeforePersistence() throws Exception {
        admin(post("/api/v1/companies").content(PAYLOAD.replace("12345678000195", "invalid")))
                .andExpect(status().isBadRequest());
        admin(post("/api/v1/companies").content(PAYLOAD.replace("\"stateRegistration\":\"ISENTO\"", "\"stateRegistration\":null")))
                .andExpect(status().isBadRequest());
        assertEquals(2, companies.count());
    }
}
