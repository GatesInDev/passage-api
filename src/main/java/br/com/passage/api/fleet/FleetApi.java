package br.com.passage.api.fleet;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.service.bus.FindBusService;
import br.com.passage.api.fleet.internal.service.driver.FindDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FleetApi {

    private final FindBusService findBusService;
    private final FindDriverService findDriverService;

    @Transactional(readOnly = true)
    public BusInfo findBus(UUID busId) {
        Bus bus = findBusService.findEntity(busId);
        return new BusInfo(bus.getId(), bus.getCompany().getId(), bus.isActive(), bus.getCapacity());
    }

    @Transactional(readOnly = true)
    public DriverInfo findDriver(UUID driverId) {
        Driver driver = findDriverService.findEntity(driverId);
        return new DriverInfo(driver.getId(), driver.getCompany().getId(), driver.isActive());
    }
}
