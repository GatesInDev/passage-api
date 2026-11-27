package br.com.passage.api.fleet;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import br.com.passage.api.fleet.internal.service.bus.FindBusService;
import br.com.passage.api.fleet.internal.service.driver.FindDriverService;
import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FleetApiTests {

    private final BusRepository busRepository = mock(BusRepository.class);
    private final DriverRepository driverRepository = mock(DriverRepository.class);
    private final IamApi iamApi = mock(IamApi.class);
    private final FleetAccessService access = new FleetAccessService(iamApi, mock(CompanyRepository.class));
    private final FleetApi api = new FleetApi(
            new FindBusService(busRepository, access), new FindDriverService(driverRepository, access));
    private final UUID companyId = UUID.randomUUID();
    private final UUID busId = UUID.randomUUID();
    private final UUID driverId = UUID.randomUUID();

    @Test
    void returnsBusContractWithOnlyActiveUndeletedSeatsInCapacity() {
        login(UserRole.FLEET_MANAGER, companyId);
        Bus bus = Bus.builder().id(busId).company(company(companyId)).isActive(true)
                .seats(List.of(
                        Seat.builder().isActive(true).build(),
                        Seat.builder().isActive(false).build(),
                        Seat.builder().isActive(true).isDeleted(true).build()))
                .build();
        when(busRepository.findVisibleById(busId, companyId)).thenReturn(Optional.of(bus));

        assertEquals(new BusInfo(busId, companyId, true, 1), api.findBus(busId));
    }

    @Test
    void returnsDriverContractIncludingInactiveState() {
        login(UserRole.FLEET_MANAGER, companyId);
        Driver driver = new Driver("Driver", "12345678901", "D", company(companyId));
        ReflectionTestUtils.setField(driver, "id", driverId);
        driver.setActive(false);
        when(driverRepository.findVisibleById(driverId, companyId)).thenReturn(Optional.of(driver));

        assertEquals(new DriverInfo(driverId, companyId, false), api.findDriver(driverId));
    }

    @Test
    void administratorCanQueryAnotherCompanyAndReceiveInactiveBus() {
        login(UserRole.ADMIN, null);
        Bus bus = Bus.builder().id(busId).company(company(companyId)).isActive(false).build();
        Driver driver = new Driver("Driver", "12345678901", "D", company(companyId));
        ReflectionTestUtils.setField(driver, "id", driverId);
        when(busRepository.findVisibleById(busId, null)).thenReturn(Optional.of(bus));
        when(driverRepository.findVisibleById(driverId, null)).thenReturn(Optional.of(driver));

        assertEquals(new BusInfo(busId, companyId, false, 0), api.findBus(busId));
        assertEquals(new DriverInfo(driverId, companyId, true), api.findDriver(driverId));
    }

    @Test
    void managerCannotFallBackToUnscopedQueriesWhenRecordsAreNotVisible() {
        login(UserRole.FLEET_MANAGER, companyId);
        when(busRepository.findVisibleById(busId, companyId)).thenReturn(Optional.empty());
        when(driverRepository.findVisibleById(driverId, companyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> api.findBus(busId));
        assertThrows(ResourceNotFoundException.class, () -> api.findDriver(driverId));
        verify(busRepository).findVisibleById(busId, companyId);
        verify(driverRepository).findVisibleById(driverId, companyId);
        verifyNoMoreInteractions(busRepository, driverRepository);
    }

    @Test
    void rejectsManagerWithoutCompanyBeforeQueryingRecords() {
        login(UserRole.FLEET_MANAGER, null);

        assertThrows(AccessDeniedException.class, () -> api.findBus(busId));
        assertThrows(AccessDeniedException.class, () -> api.findDriver(driverId));
        verifyNoInteractions(busRepository, driverRepository);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"CUSTOMER", "INSPECTOR", "TICKET_SELLER"})
    void rejectsOtherRolesEvenWhenLinkedToTheSameCompany(UserRole role) {
        login(role, companyId);

        assertThrows(AccessDeniedException.class, () -> api.findBus(busId));
        assertThrows(AccessDeniedException.class, () -> api.findDriver(driverId));
        verifyNoInteractions(busRepository, driverRepository);
    }

    private Company company(UUID id) {
        Company company = Company.builder().corporateName("Company").build();
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    private void login(UserRole role, UUID tenantId) {
        when(iamApi.currentUser()).thenReturn(new CurrentUser(UUID.randomUUID(), role, tenantId));
    }
}
