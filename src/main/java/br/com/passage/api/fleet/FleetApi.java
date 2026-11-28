package br.com.passage.api.fleet;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FleetApi {

    private final BusRepository busRepository;
    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public BusInfo findBus(UUID busId) {
        Bus bus = busRepository.findVisibleById(busId, null)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found: " + busId));
        return new BusInfo(bus.getId(), bus.getCompany().getId(), bus.isActive(), bus.getCapacity());
    }

    @Transactional(readOnly = true)
    public DriverInfo findDriver(UUID driverId) {
        Driver driver = driverRepository.findVisibleById(driverId, null)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
        return new DriverInfo(driver.getId(), driver.getCompany().getId(), driver.isActive());
    }
}
