package br.com.passage.api.fleet.internal.service.company;

import br.com.passage.api.fleet.internal.dto.company.CompanyResponse;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindAllCompanyService {

    private final CompanyRepository companyRepository;
    private final FleetAccessService fleetAccessService;

    @Transactional(readOnly = true)
    public List<CompanyResponse> execute() {
        UUID companyId = fleetAccessService.currentCompanyId();
        var companies = companyId == null
                ? companyRepository.findAllByIsDeletedFalse()
                : companyRepository.findAllByIdAndIsDeletedFalse(companyId);
        return companies.stream().map(CompanyResponse::from).toList();
    }
}
