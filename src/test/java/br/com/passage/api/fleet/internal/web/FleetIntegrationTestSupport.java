package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.domain.enums.SeatPosition;
import br.com.passage.api.fleet.internal.domain.enums.SeatQuality;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.fleet.internal.repository.SeatRepository;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.iam.internal.domain.entities.User;
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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Transactional
abstract class FleetIntegrationTestSupport {

    @Autowired protected MockMvc mvc;
    @Autowired protected CompanyRepository companies;
    @Autowired protected BusRepository buses;
    @Autowired protected DriverRepository drivers;
    @Autowired protected SeatRepository seats;
    @Autowired protected EntityManager entityManager;

    protected Company ownCompany;
    protected Company otherCompany;
    protected Bus ownBus;
    protected Bus otherBus;
    protected Driver ownDriver;
    protected Driver otherDriver;
    protected Seat ownSeat;
    protected Seat otherSeat;

    @BeforeEach
    void seedTenants() {
        ownCompany = createCompany("First Company", "11222333000181");
        otherCompany = createCompany("Second Company", "11444777000161");
        ownBus = createBus(ownCompany, "ABC1D23");
        otherBus = createBus(otherCompany, "DEF4G56");
        ownDriver = drivers.save(new Driver("First Driver", "11111111111", "D", ownCompany));
        otherDriver = drivers.save(new Driver("Second Driver", "22222222222", "E", otherCompany));
        ownSeat = createSeat(ownBus, "01", 1, 1);
        otherSeat = createSeat(otherBus, "01", 1, 1);
        entityManager.flush();
        entityManager.clear();
    }

    protected Company createCompany(String name, String document) {
        return companies.save(Company.builder()
                .corporateName(name).documentId(document).stateRegistration("ISENTO")
                .stateRegulatoryAgency("ANTT").isActive(true).build());
    }

    protected Bus createBus(Company company, String plate) {
        return buses.save(Bus.builder().company(company).code(plate).model("Model")
                .manufacturer("Manufacturer").licensePlate(plate).isActive(true).build());
    }

    protected Seat createSeat(Bus bus, String number, int row, int column) {
        return seats.save(Seat.builder().bus(bus).seatNumber(number).row(row).column(column)
                .position(SeatPosition.WINDOW).quality(SeatQuality.STANDARD).isActive(true).build());
    }

    protected RequestPostProcessor principal(UserRole role, UUID companyId) {
        User user = new User("Test User", "test@example.com", "unused", role, companyId);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return authentication(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }

    protected ResultActions manager(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON)
                .with(principal(UserRole.FLEET_MANAGER, ownCompany.getId())));
    }

    protected ResultActions admin(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON)
                .with(principal(UserRole.ADMIN, null)));
    }

    protected UUID responseId(ResultActions result) throws Exception {
        return UUID.fromString(JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id"));
    }

    protected String seatPath(Bus bus) {
        return "/api/v1/buses/" + bus.getId() + "/seats";
    }
}
