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

/**
 * Define o escopo de empresa das operações de frota para o usuário logado.
 *
 * <p>Só ADMIN e FLEET_MANAGER acessam a frota; o gestor fica restrito à própria empresa.</p>
 */
@Service
@RequiredArgsConstructor
public class FleetAccessService {

    private final IamApi iamApi;
    private final CompanyRepository companyRepository;

    /**
     * @return a empresa do gestor, ou {@code null} (sem restrição) para o ADMIN
     * @throws AccessDeniedException se o usuário não for ADMIN nem gestor com empresa vinculada
     */
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

    /**
     * Carrega a empresa, validando se o usuário logado pode acessá-la.
     *
     * @throws AccessDeniedException     se o gestor tentar acessar outra empresa
     * @throws BusinessException         se o identificador for {@code null}
     * @throws ResourceNotFoundException se a empresa não existir ou estiver excluída
     */
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

    /**
     * Empresa dona de um novo cadastro: a do gestor, ignorando a informada, ou a informada pelo ADMIN.
     *
     * @see #requireCompany(UUID)
     */
    @Transactional(readOnly = true)
    public Company resolveCompany(UUID requestedCompanyId) {
        UUID currentCompanyId = currentCompanyId();
        return requireCompany(currentCompanyId == null ? requestedCompanyId : currentCompanyId);
    }
}
