package br.com.passage.api.fleet.internal.service.bus;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.dto.bus.CreateBusRequest;
import br.com.passage.api.fleet.internal.dto.bus.BusResponse;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBusService {

    private final BusRepository busRepository;
    private final FleetAccessService fleetAccessService;

    @Transactional
    public BusResponse execute(CreateBusRequest request) {
        Company company = fleetAccessService.resolveCompany(request.companyUuid());
        if (busRepository.existsByLicensePlate(request.licensePlate())) {
            throw new BusinessException("A bus with this licensePlate already exists.");
        }
        Bus bus = Bus.builder()
                .company(company)
                .code(request.code())
                .model(request.model())
                .manufacturer(request.manufacturer())
                .licensePlate(request.licensePlate())
                .isActive(true)
                .build();
        return BusResponse.from(busRepository.save(bus));
    }
}
