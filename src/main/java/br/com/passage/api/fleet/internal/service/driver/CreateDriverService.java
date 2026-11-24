package br.com.passage.api.fleet.internal.service.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.dto.driver.CreateDriverRequest;
import br.com.passage.api.fleet.internal.dto.driver.DriverResponse;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateDriverService {

    private final DriverRepository driverRepository;
    private final FleetAccessService fleetAccessService;

    @Transactional
    public DriverResponse execute(CreateDriverRequest request) {
        Company company = fleetAccessService.resolveCompany(request.companyUuid());
        if (driverRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new BusinessException("A driver with this licenseNumber already exists.");
        }
        Driver driver = new Driver(request.name(), request.licenseNumber(), request.licenseCategory(), company);
        return DriverResponse.from(driverRepository.save(driver));
    }
}
