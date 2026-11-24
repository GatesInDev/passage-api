package br.com.passage.api.fleet.internal.service;

import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FleetAccessService {

    private final IamApi iamApi;
    private final CompanyRepository companyRepository;

    public UUID currentCompanyId() {
        var user = iamApi.currentUser();
        if (user.role() == UserRole.ADMIN) {
            return null;
        }
        if (user.role() != UserRole.FLEET_MANAGER || user.companyUuid() == null) {
            throw new AccessDeniedException("Fleet access requires an administrator or a manager linked to a company.");
        }
        return user.companyUuid();
    }

    @Transactional(readOnly = true)
    public Company requireCompany(UUID companyId) {
        UUID currentCompanyId = currentCompanyId();
        if (currentCompanyId != null && !currentCompanyId.equals(companyId)) {
            throw new AccessDeniedException("You cannot access another company's fleet.");
        }
        if (companyId == null) {
            throw new BusinessException("A company identifier is required.");
        }
        return companyRepository.findByIdAndIsDeletedFalse(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));
    }

    @Transactional(readOnly = true)
    public Company resolveCompany(UUID requestedCompanyId) {
        UUID currentCompanyId = currentCompanyId();
        return requireCompany(currentCompanyId == null ? requestedCompanyId : currentCompanyId);
    }
}
