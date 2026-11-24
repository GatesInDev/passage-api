package br.com.passage.api.fleet.internal.service.company;

import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.dto.company.CompanyResponse;
import br.com.passage.api.fleet.internal.dto.company.UpdateCompanyRequest;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UpdateCompanyService
{
    private final CompanyRepository companyRepository;
    private final IamApi iamApi;

    @Transactional
    public CompanyResponse execute(UUID id, UpdateCompanyRequest request)
    {
        var currentUser = iamApi.currentUser();
        if (currentUser.role() != UserRole.ADMIN
                && (currentUser.role() != UserRole.FLEET_MANAGER || !id.equals(currentUser.companyUuid()))) {
            throw new AccessDeniedException("Você não tem permissão para atualizar esta empresa");
        }

        Company savedCompany = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));

        if (companyRepository.existsByDocumentIdAndIdNot(request.documentId(), id)) {
            throw new BusinessException("Já existe uma empresa cadastrada com o CNPJ " + request.documentId());
        }

        savedCompany.setCorporateName(request.corporateName());
        savedCompany.setTradeName(request.tradeName());
        savedCompany.setDocumentId(request.documentId());
        savedCompany.setStateRegistration(request.stateRegistration());
        savedCompany.setStateRegulatoryAgency(request.stateRegulatoryAgency());

        companyRepository.save(savedCompany);

        return CompanyResponse.from(savedCompany);
    }
}
