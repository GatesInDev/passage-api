package br.com.passage.api.fleet.internal.service.company;

import br.com.passage.api.fleet.internal.dto.company.CompanyResponse;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindByIdCompanyService {

    private final FleetAccessService fleetAccessService;

    @Transactional(readOnly = true)
    public CompanyResponse execute(UUID id) {
        return CompanyResponse.from(fleetAccessService.requireCompany(id));
    }
}
