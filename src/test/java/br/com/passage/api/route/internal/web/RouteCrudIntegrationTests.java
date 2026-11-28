package br.com.passage.api.route.internal.web;

import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.route.internal.domain.entities.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RouteCrudIntegrationTests extends RouteIntegrationTestSupport {

    @Test
    void managerRunsTheWholeRouteLifecycle() throws Exception {
        var created = manager(post(PATH).content(createPayload("ROTA-100")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("code").value("ROTA-100"))
                .andExpect(jsonPath("companyUuid").value(ownCompany.toString()))
                .andExpect(jsonPath("isActive").value(true))
                .andExpect(jsonPath("isDeleted").value(false));

        UUID id = responseId(created);

        manager(get(PATH + "/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("code").value("ROTA-100"));

        manager(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id)).exists());

        manager(put(PATH + "/{id}", id).content(updatePayload("Joinville", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("destinationCity").value("Joinville"))
                .andExpect(jsonPath("code").value("ROTA-100"))
                .andExpect(jsonPath("companyUuid").value(ownCompany.toString()));

        manager(delete(PATH + "/{id}", id)).andExpect(status().isNoContent());
    }

    @Test
    void managerCompanyWinsOverPayload() throws Exception {
        // Mesmo informando outra empresa, a rota fica na empresa do gestor.
        manager(post(PATH).content(createPayload("ROTA-SPOOF", otherCompany)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("companyUuid").value(ownCompany.toString()));
    }

    @Test
    void administratorMustInformTheCompany() throws Exception {
        admin(post(PATH).content(createPayload("ROTA-ADM-1"))).andExpect(status().isConflict());

        admin(post(PATH).content(createPayload("ROTA-ADM-2", otherCompany)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("companyUuid").value(otherCompany.toString()));
    }

    @Test
    void managerWithoutCompanyIsDenied() throws Exception {
        as(UserRole.FLEET_MANAGER, null, get(PATH)).andExpect(status().isForbidden());
        as(UserRole.FLEET_MANAGER, null, get(PATH + "/{id}", ownRoute.getId())).andExpect(status().isForbidden());
        as(UserRole.FLEET_MANAGER, null, post(PATH).content(createPayload("ROTA-SEM-EMPRESA")))
                .andExpect(status().isConflict());
    }

    @Test
    void managerNeverReachesAnotherCompanyRoutes() throws Exception {
        UUID foreign = otherRoute.getId();

        // A listagem traz apenas a própria empresa.
        manager(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(ownRoute.getId())).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(foreign)).doesNotExist());

        // A rota alheia se comporta como inexistente.
        manager(get(PATH + "/{id}", foreign)).andExpect(status().isNotFound());
        manager(put(PATH + "/{id}", foreign).content(updatePayload("Invasão", true)))
                .andExpect(status().isNotFound());
        manager(delete(PATH + "/{id}", foreign)).andExpect(status().isNotFound());

        Route stored = routes.findById(foreign).orElseThrow();
        assertEquals("São Paulo", stored.getDestinationCity());
        assertFalse(stored.isDeleted());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"ADMIN", "CUSTOMER", "TICKET_SELLER", "INSPECTOR"})
    void otherProfilesSeeTheRoutesOfEveryCompany(UserRole role) throws Exception {
        as(role, get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(ownRoute.getId())).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(otherRoute.getId())).exists());

        as(role, get(PATH + "/{id}", otherRoute.getId())).andExpect(status().isOk());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"CUSTOMER", "TICKET_SELLER", "INSPECTOR"})
    void onlyAdministratorsAndManagersChangeRoutes(UserRole role) throws Exception {
        UUID id = ownRoute.getId();

        as(role, post(PATH).content(createPayload("ROTA-" + role))).andExpect(status().isForbidden());
        as(role, put(PATH + "/{id}", id).content(updatePayload("Joinville", true))).andExpect(status().isForbidden());
        as(role, delete(PATH + "/{id}", id)).andExpect(status().isForbidden());

        assertEquals("São Paulo", routes.findById(id).orElseThrow().getDestinationCity());
    }

    @Test
    void softDeletedRouteDisappearsFromTheApiButRemainsStored() throws Exception {
        UUID id = ownRoute.getId();

        manager(delete(PATH + "/{id}", id)).andExpect(status().isNoContent());

        // A rota excluída deixa de existir para a API...
        admin(get(PATH + "/{id}", id)).andExpect(status().isNotFound());
        manager(put(PATH + "/{id}", id).content(updatePayload("Joinville", true)))
                .andExpect(status().isNotFound());
        manager(delete(PATH + "/{id}", id)).andExpect(status().isNotFound());
        admin(get(PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id)).doesNotExist());

        // ...mas continua no banco.
        Route stored = routes.findById(id).orElseThrow();
        assertTrue(stored.isDeleted());
        assertFalse(stored.isActive());
        assertEquals("São Paulo", stored.getDestinationCity(), "o PUT não pode alterar rota excluída");
    }

    @Test
    void unknownIdentifierIsReportedAsNotFound() throws Exception {
        UUID unknown = UUID.randomUUID();

        admin(get(PATH + "/{id}", unknown)).andExpect(status().isNotFound());
        admin(put(PATH + "/{id}", unknown).content(updatePayload("Joinville", true)))
                .andExpect(status().isNotFound());
        admin(delete(PATH + "/{id}", unknown)).andExpect(status().isNotFound());
    }

    @Test
    void duplicatedCodeIsRejectedAsBusinessConflict() throws Exception {
        manager(post(PATH).content(createPayload("ROTA-DUP"))).andExpect(status().isCreated());
        manager(post(PATH).content(createPayload("ROTA-DUP"))).andExpect(status().isConflict());
    }

    @Test
    void invalidPayloadIsRejectedWithFieldDetails() throws Exception {
        String invalid = """
                {"code":"","name":"X","originCity":"Florianópolis","originState":"Santa Catarina",
                 "destinationCity":"Curitiba","destinationState":"PR","distanceKm":-10,
                 "estimatedDurationMinutes":0,"serviceScope":"INTERSTATE","regulatoryAgency":"ANTT"}
                """;

        manager(post(PATH).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("invalidFields").isArray());
    }

    @Test
    void routeCodeIsNotEditableAfterCreation() throws Exception {
        UUID id = ownRoute.getId();

        // O payload de atualização não possui o campo "code": mesmo enviado, é ignorado.
        String withCode = updatePayload("Joinville", true).replaceFirst("\\{", "{\"code\":\"ROTA-HACK\",");

        manager(put(PATH + "/{id}", id).content(withCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("ROTA-SEED-001"))
                .andExpect(jsonPath("destinationCity").value("Joinville"));
    }
}
