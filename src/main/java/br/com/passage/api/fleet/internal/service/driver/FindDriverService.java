package br.com.passage.api.fleet.internal.service.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.dto.driver.DriverResponse;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Consulta motoristas no escopo do usuário logado (ver {@link FleetAccessService}). */
@Service
@RequiredArgsConstructor
public class FindDriverService {

    private final DriverRepository driverRepository;
    private final FleetAccessService fleetAccessService;

    /** @throws ResourceNotFoundException se o motorista não existir, estiver excluído ou for de outra empresa */
    @Transactional(readOnly = true)
    public Driver findEntity(UUID id) {
        UUID companyId = fleetAccessService.currentCompanyId();
        return driverRepository.findVisibleById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
    }

    @Transactional(readOnly = true)
    public DriverResponse execute(UUID id) {
        return DriverResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> executeAll() {
        UUID companyId = fleetAccessService.currentCompanyId();
        return driverRepository.findAllVisible(companyId).stream()
                .map(DriverResponse::from)
                .toList();
    }
}
