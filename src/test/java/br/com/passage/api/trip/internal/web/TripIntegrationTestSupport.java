package br.com.passage.api.trip.internal.web;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.domain.enums.RouteScope;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.trip.enums.TripStatus;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import br.com.passage.api.trip.internal.repository.TripRepository;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Base dos testes de integração do módulo de viagens.
 *
 * <p>Cada teste nasce com duas empresas, cada uma com rota, ônibus, motorista
 * e uma viagem agendada, além de um ônibus e um motorista inativos da própria
 * empresa. As alterações são revertidas ao fim de cada teste pela transação.</p>
 */
@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Transactional
abstract class TripIntegrationTestSupport {

    protected static final String PATH = "/api/v1/trips";

    @Autowired protected MockMvc mvc;
    @Autowired protected TripRepository trips;
    @Autowired protected RouteRepository routes;
    @Autowired protected CompanyRepository companies;
    @Autowired protected BusRepository buses;
    @Autowired protected DriverRepository drivers;
    @Autowired protected EntityManager entityManager;

    protected Company ownCompany;
    protected Company otherCompany;
    protected Route ownRoute;
    protected Route otherRoute;
    protected Bus ownBus;
    protected Bus otherBus;
    protected Bus inactiveBus;
    protected Driver ownDriver;
    protected Driver otherDriver;
    protected Driver inactiveDriver;

    /** Viagem agendada da própria empresa, disponível em todos os testes. */
    protected Trip ownTrip;

    /** Viagem de outra empresa, que o gestor não deve enxergar. */
    protected Trip otherTrip;

    /** Partida das viagens de exemplo, sempre no futuro. */
    protected Instant departure;

    /**
     * Horário livre, dois dias depois das viagens de exemplo, usado pelos payloads
     * para não esbarrar no conflito de agenda do ônibus e do motorista.
     */
    protected Instant freeSlot;

    @BeforeEach
    void seedTrips() {
        departure = Instant.now().plus(10, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        freeSlot = departure.plus(2, ChronoUnit.DAYS);

        ownCompany = saveCompany("Viação Própria", "48130527000167");
        otherCompany = saveCompany("Viação Concorrente", "73019462000124");

        ownRoute = saveRoute("TRIP-ROTA-001", ownCompany.getId());
        otherRoute = saveRoute("TRIP-ROTA-002", otherCompany.getId());

        ownBus = saveBus(ownCompany, "TRP1A01", true);
        otherBus = saveBus(otherCompany, "TRP1A02", true);
        inactiveBus = saveBus(ownCompany, "TRP1A03", false);

        ownDriver = saveDriver(ownCompany, "90000000001", true);
        otherDriver = saveDriver(otherCompany, "90000000002", true);
        inactiveDriver = saveDriver(ownCompany, "90000000003", false);

        ownTrip = saveTrip("VIAGEM-SEED-001", ownRoute, ownBus, ownDriver, TripStatus.SCHEDULED);
        otherTrip = saveTrip("VIAGEM-SEED-002", otherRoute, otherBus, otherDriver, TripStatus.SCHEDULED);

        entityManager.flush();
        entityManager.clear();
    }

    protected Company saveCompany(String name, String document) {
        return companies.save(Company.builder()
                .corporateName(name + " S.A.").tradeName(name).documentId(document)
                .stateRegistration("ISENTO").stateRegulatoryAgency("ANTT").isActive(true).build());
    }

    protected Route saveRoute(String code, UUID companyUuid) {
        return routes.save(Route.builder()
                .code(code).companyUuid(companyUuid).name("Florianópolis - Curitiba")
                .originCity("Florianópolis").originState("SC")
                .destinationCity("Curitiba").destinationState("PR")
                .distanceKm(new BigDecimal("300.00")).estimatedDurationMinutes(300)
                .serviceScope(RouteScope.INTERSTATE).regulatoryAgency("ANTT").isActive(true).build());
    }

    protected Bus saveBus(Company company, String plate, boolean active) {
        return buses.save(Bus.builder().company(company).code(plate).model("Paradiso 1800")
                .manufacturer("Marcopolo").licensePlate(plate).isActive(active).build());
    }

    protected Driver saveDriver(Company company, String license, boolean active) {
        Driver driver = new Driver("Motorista " + license, license, "D", company);
        driver.setActive(active);
        return drivers.save(driver);
    }

    protected Trip saveTrip(String code, Route route, Bus bus, Driver driver, TripStatus status) {
        return trips.save(Trip.builder()
                .code(code).routeId(route.getId()).companyUuid(route.getCompanyUuid())
                .busId(bus.getId()).driverId(driver.getId())
                .departureTime(departure).arrivalTime(departure.plus(5, ChronoUnit.HOURS))
                .status(status).price(new BigDecimal("150.00")).isActive(true).build());
    }

    protected String createPayload(String code, UUID routeId, UUID busId, UUID driverId) {
        return createPayload(code, routeId, busId, driverId, freeSlot, freeSlot.plus(5, ChronoUnit.HOURS));
    }

    protected String createPayload(String code, UUID routeId, UUID busId, UUID driverId,
                                   Instant departureTime, Instant arrivalTime) {
        return """
                {"code":"%s","routeId":"%s","busId":"%s","driverId":"%s",
                 "departureTime":"%s","arrivalTime":"%s","price":189.90}
                """.formatted(code, routeId, busId, driverId, departureTime, arrivalTime);
    }

    /** Payload de criação na rota, ônibus e motorista da própria empresa. */
    protected String ownCreatePayload(String code) {
        return createPayload(code, ownRoute.getId(), ownBus.getId(), ownDriver.getId());
    }

    protected String updatePayload(TripStatus status) {
        return updatePayload(ownBus.getId(), ownDriver.getId(), status, "210.00");
    }

    protected String updatePayload(UUID busId, UUID driverId, TripStatus status, String price) {
        return updatePayload(busId, driverId, status, price, freeSlot, freeSlot.plus(4, ChronoUnit.HOURS));
    }

    protected String updatePayload(UUID busId, UUID driverId, TripStatus status, String price,
                                   Instant departureTime, Instant arrivalTime) {
        return """
                {"busId":"%s","driverId":"%s","departureTime":"%s","arrivalTime":"%s",
                 "price":%s,"status":"%s"}
                """.formatted(busId, driverId, departureTime, arrivalTime, price, status);
    }

    protected RequestPostProcessor principal(UserRole role) {
        // O administrador não é vinculado a empresa: enxerga todas.
        UUID companyUuid = role == UserRole.ADMIN ? null : ownCompany.getId();
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

    protected UUID responseId(ResultActions result) throws Exception {
        return UUID.fromString(
                JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id"));
    }
}
