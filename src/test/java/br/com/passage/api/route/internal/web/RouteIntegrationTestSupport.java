package br.com.passage.api.route.internal.web;

import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.domain.enums.RouteScope;
import br.com.passage.api.route.internal.repository.RouteRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Base dos testes de integração do módulo de rotas.
 *
 * <p>Cada teste nasce com uma rota da própria empresa e uma de outra empresa,
 * e com atalhos para executar requisições como cada perfil de acesso. As
 * alterações são revertidas ao fim de cada teste pela transação.</p>
 */
@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Transactional
abstract class RouteIntegrationTestSupport {

    protected static final String PATH = "/api/v1/routes";

    @Autowired protected MockMvc mvc;
    @Autowired protected RouteRepository routes;
    @Autowired protected EntityManager entityManager;

    /** Empresa do gestor autenticado nos testes. */
    protected final UUID ownCompany = UUID.randomUUID();

    /** Empresa concorrente, usada para verificar o isolamento. */
    protected final UUID otherCompany = UUID.randomUUID();

    /** Rota da própria empresa, disponível em todos os testes. */
    protected Route ownRoute;

    /** Rota de outra empresa, que o gestor não deve enxergar. */
    protected Route otherRoute;

    @BeforeEach
    void seedRoutes() {
        ownRoute = saveRoute("ROTA-SEED-001", ownCompany);
        otherRoute = saveRoute("ROTA-SEED-002", otherCompany);

        entityManager.flush();
        entityManager.clear();
    }

    protected Route saveRoute(String code, UUID companyUuid) {
        return routes.save(Route.builder()
                .code(code)
                .companyUuid(companyUuid)
                .name("Florianópolis - São Paulo")
                .originCity("Florianópolis")
                .originState("SC")
                .destinationCity("São Paulo")
                .destinationState("SP")
                .distanceKm(new BigDecimal("705.40"))
                .estimatedDurationMinutes(720)
                .serviceScope(RouteScope.INTERSTATE)
                .regulatoryAgency("ANTT")
                .isActive(true)
                .build());
    }

    protected String createPayload(String code) {
        return createPayload(code, null);
    }

    protected String createPayload(String code, UUID companyUuid) {
        String company = companyUuid == null ? "null" : "\"" + companyUuid + "\"";
        return """
                {"code":"%s","companyUuid":%s,"name":"Florianópolis - Curitiba via BR-101",
                 "originCity":"Florianópolis","originState":"SC","destinationCity":"Curitiba",
                 "destinationState":"PR","distanceKm":300.50,"estimatedDurationMinutes":300,
                 "serviceScope":"INTERSTATE","regulatoryAgency":"ANTT"}
                """.formatted(code, company);
    }

    protected String updatePayload(String destinationCity, boolean isActive) {
        return """
                {"name":"Florianópolis - %s","originCity":"Florianópolis","originState":"SC",
                 "destinationCity":"%s","destinationState":"PR","distanceKm":420.00,
                 "estimatedDurationMinutes":400,"serviceScope":"INTERSTATE",
                 "regulatoryAgency":"ANTT","isActive":%s}
                """.formatted(destinationCity, destinationCity, isActive);
    }

    protected RequestPostProcessor principal(UserRole role) {
        // O administrador não é vinculado a empresa: enxerga todas.
        UUID companyUuid = role == UserRole.ADMIN ? null : ownCompany;
        return principal(role, companyUuid);
    }

    protected RequestPostProcessor principal(UserRole role, UUID companyUuid) {
        User user = new User("Test User", role.name().toLowerCase() + "@example.com", "unused", role, companyUuid);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return authentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }

    protected ResultActions as(UserRole role, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).with(principal(role)));
    }

    protected ResultActions as(UserRole role, UUID companyUuid, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).with(principal(role, companyUuid)));
    }

    protected ResultActions admin(MockHttpServletRequestBuilder request) throws Exception {
        return as(UserRole.ADMIN, request);
    }

    protected ResultActions manager(MockHttpServletRequestBuilder request) throws Exception {
        return as(UserRole.FLEET_MANAGER, request);
    }

    protected ResultActions customer(MockHttpServletRequestBuilder request) throws Exception {
        return as(UserRole.CUSTOMER, request);
    }

    protected UUID responseId(ResultActions result) throws Exception {
        return UUID.fromString(
                JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id"));
    }
}
