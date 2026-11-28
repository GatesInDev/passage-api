package br.com.passage.api.route.internal.web;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Regras de coerência entre origem, destino e abrangência da rota.
 */
class RouteConsistencyIntegrationTests extends RouteIntegrationTestSupport {

    private String payload(String code, String originCity, String originState,
                           String destinationCity, String destinationState, String scope) {
        return """
                {"code":"%s","name":"Rota de teste","originCity":"%s","originState":"%s",
                 "destinationCity":"%s","destinationState":"%s","distanceKm":120.00,
                 "estimatedDurationMinutes":120,"serviceScope":"%s","regulatoryAgency":"ANTT"}
                """.formatted(code, originCity, originState, destinationCity, destinationState, scope);
    }

    private String updatePayload(String destinationCity, String destinationState, String scope) {
        return """
                {"name":"Rota alterada","originCity":"Florianópolis","originState":"SC",
                 "destinationCity":"%s","destinationState":"%s","distanceKm":120.00,
                 "estimatedDurationMinutes":120,"serviceScope":"%s","regulatoryAgency":"ANTT","isActive":true}
                """.formatted(destinationCity, destinationState, scope);
    }

    @ParameterizedTest(name = "{0} -> {1} é a mesma cidade")
    @CsvSource({
            "Curitiba, Curitiba",
            "Curitiba, curitiba",
            "Curitiba, 'Curitiba '"
    })
    void originAndDestinationCannotBeTheSame(String origin, String destination) throws Exception {
        manager(post(PATH).content(payload("ROTA-IGUAL", origin, "PR", destination, "PR", "INTRASTATE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("A origem e o destino da rota não podem ser iguais."));
    }

    @ParameterizedTest(name = "{0} entre estados diferentes é recusada")
    @CsvSource({"INTRASTATE", "METROPOLITAN"})
    void stateAndMetropolitanRoutesStayInOneState(String scope) throws Exception {
        manager(post(PATH).content(payload("ROTA-" + scope, "Florianópolis", "SC", "Curitiba", "PR", scope)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value(
                        "Rotas estaduais e metropolitanas devem começar e terminar no mesmo estado."));
    }

    @Test
    void interstateRouteMustCrossStates() throws Exception {
        manager(post(PATH).content(payload("ROTA-INTER", "Florianópolis", "SC", "Joinville", "SC", "INTERSTATE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail").value("Rotas interestaduais devem ligar estados diferentes."));
    }

    @ParameterizedTest(name = "{4}: {0}/{1} -> {2}/{3} é aceita")
    @CsvSource({
            "Florianópolis, SC, Joinville, SC, INTRASTATE",
            "Florianópolis, SC, São José, SC, METROPOLITAN",
            "Florianópolis, SC, Curitiba, PR, INTERSTATE",
            "Foz do Iguaçu, PR, Assunção, PR, INTERNATIONAL"
    })
    void coherentRoutesAreAccepted(String originCity, String originState,
                                   String destinationCity, String destinationState, String scope) throws Exception {
        manager(post(PATH).content(payload("ROTA-OK-" + scope, originCity, originState,
                destinationCity, destinationState, scope)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateIsValidatedAndChangesNothingWhenIncoherent() throws Exception {
        UUID id = ownRoute.getId();

        manager(put(PATH + "/{id}", id).content(updatePayload("Joinville", "SC", "INTERSTATE")))
                .andExpect(status().isConflict());
        manager(put(PATH + "/{id}", id).content(updatePayload("Florianópolis", "SC", "INTRASTATE")))
                .andExpect(status().isConflict());

        var stored = routes.findById(id).orElseThrow();
        assertEquals("São Paulo", stored.getDestinationCity());
        assertEquals("SP", stored.getDestinationState());
    }
}
