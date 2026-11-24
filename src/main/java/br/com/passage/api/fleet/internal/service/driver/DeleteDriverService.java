package br.com.passage.api.fleet.internal.service.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteDriverService {

    private final DriverRepository driverRepository;
    private final FindDriverService findDriverService;

    @Transactional
    public void execute(UUID id) {
        Driver driver = findDriverService.findEntity(id);
        driver.setDeleted(true);
        driver.setActive(false);
        driverRepository.save(driver);
    }
}
