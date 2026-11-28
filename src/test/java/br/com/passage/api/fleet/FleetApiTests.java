package br.com.passage.api.fleet;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.fleet.internal.repository.SeatRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FleetApiTests {

    private final BusRepository busRepository = mock(BusRepository.class);
    private final DriverRepository driverRepository = mock(DriverRepository.class);
    private final SeatRepository seatRepository = mock(SeatRepository.class);
    private final FleetApi api = new FleetApi(busRepository, driverRepository, seatRepository);
    private final UUID companyId = UUID.randomUUID();
    private final UUID busId = UUID.randomUUID();
    private final UUID driverId = UUID.randomUUID();

    @Test
    void returnsBusContractWithOnlyActiveUndeletedSeatsInCapacity() {
        Bus bus = Bus.builder().id(busId).company(company(companyId)).isActive(true)
                .seats(List.of(
                        Seat.builder().isActive(true).build(),
                        Seat.builder().isActive(false).build(),
                        Seat.builder().isActive(true).isDeleted(true).build()))
                .build();
        when(busRepository.findVisibleById(busId, null)).thenReturn(Optional.of(bus));

        assertEquals(new BusInfo(busId, companyId, true, 1), api.findBus(busId));
    }

    @Test
    void returnsDriverContractIncludingInactiveState() {
        Driver driver = new Driver("Driver", "12345678901", "D", company(companyId));
        ReflectionTestUtils.setField(driver, "id", driverId);
        driver.setActive(false);
        when(driverRepository.findVisibleById(driverId, null)).thenReturn(Optional.of(driver));

        assertEquals(new DriverInfo(driverId, companyId, false), api.findDriver(driverId));
    }

    @Test
    void returnsInactiveBusAndActiveDriver() {
        Bus bus = Bus.builder().id(busId).company(company(companyId)).isActive(false).build();
        Driver driver = new Driver("Driver", "12345678901", "D", company(companyId));
        ReflectionTestUtils.setField(driver, "id", driverId);
        when(busRepository.findVisibleById(busId, null)).thenReturn(Optional.of(bus));
        when(driverRepository.findVisibleById(driverId, null)).thenReturn(Optional.of(driver));

        assertEquals(new BusInfo(busId, companyId, false, 0), api.findBus(busId));
        assertEquals(new DriverInfo(driverId, companyId, true), api.findDriver(driverId));
    }

    @Test
    void throwsNotFoundWhenRecordsAreNotAvailable() {
        when(busRepository.findVisibleById(busId, null)).thenReturn(Optional.empty());
        when(driverRepository.findVisibleById(driverId, null)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> api.findBus(busId));
        assertThrows(ResourceNotFoundException.class, () -> api.findDriver(driverId));
        verify(busRepository).findVisibleById(busId, null);
        verify(driverRepository).findVisibleById(driverId, null);
        verifyNoMoreInteractions(busRepository, driverRepository);
    }

    private Company company(UUID id) {
        Company company = Company.builder().corporateName("Company").build();
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }
}
