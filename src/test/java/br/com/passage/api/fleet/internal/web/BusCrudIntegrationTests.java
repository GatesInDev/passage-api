package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.iam.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BusCrudIntegrationTests extends FleetIntegrationTestSupport {

    private static final String FIELDS = """
            "code":"B-100","model":"Model","manufacturer":"Manufacturer","licensePlate":"XYZ9A87"
            """.strip();

    @Test
    void managerCannotReadUpdateOrDeleteAnotherTenantRecord() throws Exception {
        manager(get("/api/v1/buses")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ownBus.getId().toString()));
        manager(get("/api/v1/buses/{id}", otherBus.getId())).andExpect(status().isNotFound());
        manager(put("/api/v1/buses/{id}", otherBus.getId()).content("{" + FIELDS + ",\"isActive\":true}"))
                .andExpect(status().isNotFound());
        manager(delete("/api/v1/buses/{id}", otherBus.getId())).andExpect(status().isNotFound());
        assertFalse(buses.findById(otherBus.getId()).orElseThrow().isDeleted());
    }

    @Test
    void managerCreationUsesAuthenticatedTenantDespiteSpoofedPayload() throws Exception {
        var response = manager(post("/api/v1/buses").content("{" + FIELDS
                + ",\"companyUuid\":\"" + otherCompany.getId() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("companyUuid").value(ownCompany.getId().toString()))
                .andExpect(jsonPath("isActive").value(true));
        UUID id = responseId(response);
        manager(put("/api/v1/buses/{id}", id).content("{" + FIELDS.replace("XYZ9A87", "NEW1A23")
                + ",\"isActive\":false,\"companyUuid\":\"" + otherCompany.getId() + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("licensePlate").value("NEW1A23"))
                .andExpect(jsonPath("companyUuid").value(ownCompany.getId().toString()))
                .andExpect(jsonPath("isActive").value(false));
    }

    @Test
    void administratorCanReadAndManageMultipleTenants() throws Exception {
        admin(get("/api/v1/buses")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        admin(get("/api/v1/buses/{id}", otherBus.getId())).andExpect(status().isOk());
        admin(put("/api/v1/buses/{id}", otherBus.getId()).content("{" + FIELDS + ",\"isActive\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("companyUuid").value(otherCompany.getId().toString()));
        admin(post("/api/v1/buses").content("{" + FIELDS.replace("XYZ9A87", "NEW1A23")
                + ",\"companyUuid\":\"" + otherCompany.getId() + "\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("companyUuid").value(otherCompany.getId().toString()));
        admin(delete("/api/v1/buses/{id}", otherBus.getId())).andExpect(status().isNoContent());
    }

    @Test
    void missingOrDeletedCompanyCannotBeUsedForCreation() throws Exception {
        admin(post("/api/v1/buses").content("{" + FIELDS + "}")).andExpect(status().isConflict());
        admin(post("/api/v1/buses").content("{" + FIELDS + ",\"companyUuid\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isNotFound());
        admin(delete("/api/v1/companies/{id}", ownCompany.getId())).andExpect(status().isNoContent());
        manager(post("/api/v1/buses").content("{" + FIELDS + "}")).andExpect(status().isNotFound());
    }

    @Test
    void managerWithoutTenantCannotFallBackToGlobalAccess() throws Exception {
        mvc.perform(get("/api/v1/buses").with(principal(UserRole.FLEET_MANAGER, null)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/buses/{id}", ownBus.getId()).with(principal(UserRole.FLEET_MANAGER, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void softDeletionHidesRecordsButRetainsUniqueIdentifier() throws Exception {
        UUID id = responseId(manager(post("/api/v1/buses").content("{" + FIELDS + "}"))
                .andExpect(status().isCreated()));
        manager(delete("/api/v1/buses/{id}", id)).andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();
        var deleted = buses.findById(id).orElseThrow();
        assertTrue(deleted.isDeleted());
        assertFalse(deleted.isActive());
        admin(get("/api/v1/buses/{id}", id)).andExpect(status().isNotFound());
        manager(get("/api/v1/buses")).andExpect(jsonPath("$.length()").value(1));
        manager(put("/api/v1/buses/{id}", id).content("{" + FIELDS + ",\"isActive\":true}"))
                .andExpect(status().isNotFound());
        manager(delete("/api/v1/buses/{id}", id)).andExpect(status().isNotFound());
        manager(post("/api/v1/buses").content("{" + FIELDS + "}")).andExpect(status().isConflict());
    }

    @Test
    void duplicateAndInvalidPayloadsAreRejected() throws Exception {
        manager(post("/api/v1/buses").content("{" + FIELDS + "}")).andExpect(status().isCreated());
        manager(post("/api/v1/buses").content("{" + FIELDS + "}")).andExpect(status().isConflict());
        manager(put("/api/v1/buses/{id}", ownBus.getId()).content("{" + FIELDS + ",\"isActive\":true}"))
                .andExpect(status().isConflict());
        manager(post("/api/v1/buses").content("{}")).andExpect(status().isBadRequest());
        manager(put("/api/v1/buses/{id}", ownBus.getId()).content("{" + FIELDS + "}"))
                .andExpect(status().isBadRequest());
        assertEquals(3, buses.count());
    }
}
