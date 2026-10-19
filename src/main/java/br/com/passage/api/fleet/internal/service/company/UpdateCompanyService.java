package br.com.passage.api.fleet.internal.service.company;

import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.dto.company.CompanyResponse;
import br.com.passage.api.fleet.internal.dto.company.UpdateCompanyRequest;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UpdateCompanyService
{
    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse execute(UUID id, UpdateCompanyRequest request)
    {
        Company savedCompany = companyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Não foi possível encontrar uma companhia com o identificador: " + id));

        savedCompany.setCorporateName(request.corporateName());
        savedCompany.setTradeName(request.tradeName());
        savedCompany.setDocumentId(request.documentId());
        savedCompany.setStateRegistration(request.stateRegistration());
        savedCompany.setStateRegulatoryAgency(request.stateRegulatoryAgency());

        companyRepository.save(savedCompany);

        return new CompanyResponse(
                savedCompany.getId(),
                savedCompany.getCreatedAt(),
                savedCompany.getUpdatedAt(),
                savedCompany.isActive(),
                savedCompany.isDeleted(),
                savedCompany.getCorporateName(),
                savedCompany.getTradeName(),
                savedCompany.getDocumentId(),
                savedCompany.getStateRegistration(),
                savedCompany.getStateRegulatoryAgency()
        );
    }
}
