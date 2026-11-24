package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.iam.enums.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FleetAuthorizationIntegrationTests extends FleetIntegrationTestSupport {

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"CUSTOMER", "INSPECTOR", "TICKET_SELLER"})
    void otherRolesCannotAccessFleetEvenWhenLinkedToTheSameTenant(UserRole role) throws Exception {
        for (String path : new String[]{"/api/v1/companies", "/api/v1/buses", "/api/v1/drivers", seatPath(ownBus)}) {
            mvc.perform(get(path).with(principal(role, ownCompany.getId()))).andExpect(status().isForbidden());
        }
        for (String path : new String[]{"/api/v1/companies/" + ownCompany.getId(),
                "/api/v1/buses/" + ownBus.getId(), "/api/v1/drivers/" + ownDriver.getId(),
                seatPath(ownBus) + "/" + ownSeat.getId()}) {
            mvc.perform(get(path).with(principal(role, ownCompany.getId()))).andExpect(status().isForbidden());
            mvc.perform(delete(path).with(principal(role, ownCompany.getId()))).andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/v1/buses").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"B-1","model":"Model","manufacturer":"Manufacturer","licensePlate":"XYZ9A87"}
                """).with(principal(role, ownCompany.getId()))).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/drivers/{id}", ownDriver.getId()).contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Updated Driver","licenseNumber":"11111111111","licenseCategory":"D","isActive":true}
                """).with(principal(role, ownCompany.getId()))).andExpect(status().isForbidden());
    }
}
