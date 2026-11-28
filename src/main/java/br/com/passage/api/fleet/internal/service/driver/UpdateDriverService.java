package br.com.passage.api.fleet.internal.service.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.dto.driver.UpdateDriverRequest;
import br.com.passage.api.fleet.internal.dto.driver.DriverResponse;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Atualiza um motorista visível ao usuário logado; a empresa não muda. */
@Service
@RequiredArgsConstructor
public class UpdateDriverService {

    private final DriverRepository driverRepository;
    private final FindDriverService findDriverService;

    @Transactional
    public DriverResponse execute(UUID id, UpdateDriverRequest request) {
        Driver driver = findDriverService.findEntity(id);
        if (driverRepository.existsByLicenseNumberAndIdNot(request.licenseNumber(), id)) {
            throw new BusinessException("A driver with this licenseNumber already exists.");
        }
        driver.setName(request.name());
        driver.setLicenseNumber(request.licenseNumber());
        driver.setLicenseCategory(request.licenseCategory());
        driver.setActive(request.isActive());
        return DriverResponse.from(driverRepository.save(driver));
    }
}
