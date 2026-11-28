package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.BusInfo;
import br.com.passage.api.fleet.DriverInfo;
import br.com.passage.api.fleet.FleetApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetApiIntegrationTests extends FleetIntegrationTestSupport {

    @Autowired private FleetApi fleetApi;

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void internalCallsReturnFleetDetailsWithoutAuthentication() {
        assertEquals(new BusInfo(ownBus.getId(), ownCompany.getId(), true, 1),
                fleetApi.findBus(ownBus.getId()));
        assertEquals(new DriverInfo(ownDriver.getId(), ownCompany.getId(), true),
                fleetApi.findDriver(ownDriver.getId()));
        assertTrue(fleetApi.seatBelongsToBus(ownSeat.getId(), ownBus.getId()));
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void internalCallsReturnResourceOwnershipRegardlessOfCallerRoleAndTenant(UserRole role) {
        authenticate(role, ownCompany.getId());

        assertEquals(new BusInfo(otherBus.getId(), otherCompany.getId(), true, 1),
                fleetApi.findBus(otherBus.getId()));
        assertEquals(new DriverInfo(otherDriver.getId(), otherCompany.getId(), true),
                fleetApi.findDriver(otherDriver.getId()));
        assertTrue(fleetApi.seatBelongsToBus(otherSeat.getId(), otherBus.getId()));
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void internalCallsDoNotRequireTheCallerToHaveACompany(UserRole role) {
        authenticate(role, null);

        assertEquals(ownCompany.getId(), fleetApi.findBus(ownBus.getId()).companyUuid());
        assertEquals(ownCompany.getId(), fleetApi.findDriver(ownDriver.getId()).companyUuid());
        assertTrue(fleetApi.seatBelongsToBus(ownSeat.getId(), ownBus.getId()));
    }

    @Test
    void seatMembershipRequiresAnExistingSeatAssignedToTheRequestedBus() {
        assertTrue(fleetApi.seatBelongsToBus(ownSeat.getId(), ownBus.getId()));
        assertFalse(fleetApi.seatBelongsToBus(ownSeat.getId(), otherBus.getId()));
        assertFalse(fleetApi.seatBelongsToBus(UUID.randomUUID(), ownBus.getId()));
        assertFalse(fleetApi.seatBelongsToBus(ownSeat.getId(), UUID.randomUUID()));
    }

    @Test
    void deletedSeatsDoNotBelongToTheBus() {
        seats.findById(ownSeat.getId()).orElseThrow().setDeleted(true);
        entityManager.flush();
        entityManager.clear();

        assertFalse(fleetApi.seatBelongsToBus(ownSeat.getId(), ownBus.getId()));
        assertTrue(fleetApi.seatBelongsToBus(otherSeat.getId(), otherBus.getId()));
    }

    @Test
    void inactiveSeatsStillBelongToTheBus() {
        seats.findById(ownSeat.getId()).orElseThrow().setActive(false);
        entityManager.flush();
        entityManager.clear();

        assertTrue(fleetApi.seatBelongsToBus(ownSeat.getId(), ownBus.getId()));
    }

    @Test
    void inactiveResourcesRemainAvailableWithTheirActualStatus() {
        buses.findById(ownBus.getId()).orElseThrow().setActive(false);
        drivers.findById(ownDriver.getId()).orElseThrow().setActive(false);
        entityManager.flush();
        entityManager.clear();

        assertFalse(fleetApi.findBus(ownBus.getId()).active());
        assertFalse(fleetApi.findDriver(ownDriver.getId()).active());
        assertTrue(fleetApi.findBus(otherBus.getId()).active());
        assertTrue(fleetApi.findDriver(otherDriver.getId()).active());
    }

    @Test
    void deletedResourcesAreNotReturned() {
        buses.findById(ownBus.getId()).orElseThrow().setDeleted(true);
        drivers.findById(ownDriver.getId()).orElseThrow().setDeleted(true);
        entityManager.flush();
        entityManager.clear();

        assertThrows(ResourceNotFoundException.class, () -> fleetApi.findBus(ownBus.getId()));
        assertThrows(ResourceNotFoundException.class, () -> fleetApi.findDriver(ownDriver.getId()));
    }

    @Test
    void resourcesOfADeletedCompanyAreNotReturned() {
        companies.findById(ownCompany.getId()).orElseThrow().setDeleted(true);
        entityManager.flush();
        entityManager.clear();

        assertThrows(ResourceNotFoundException.class, () -> fleetApi.findBus(ownBus.getId()));
        assertThrows(ResourceNotFoundException.class, () -> fleetApi.findDriver(ownDriver.getId()));
        assertEquals(otherCompany.getId(), fleetApi.findBus(otherBus.getId()).companyUuid());
        assertEquals(otherCompany.getId(), fleetApi.findDriver(otherDriver.getId()).companyUuid());
    }

    @Test
    void missingResourcesAreNotReturned() {
        assertThrows(ResourceNotFoundException.class, () -> fleetApi.findBus(UUID.randomUUID()));
        assertThrows(ResourceNotFoundException.class, () -> fleetApi.findDriver(UUID.randomUUID()));
    }

    private void authenticate(UserRole role, UUID companyId) {
        User user = new User("Internal Caller", "internal@example.com", "unused", role, companyId);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }
}
