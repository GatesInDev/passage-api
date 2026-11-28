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
 * Serviço responsável por definir o escopo de empresa das operações de frota.
 *
 * <p>A frota é acessada apenas pelos perfis ADMIN e FLEET_MANAGER:</p>
 *
 * <ul>
 *     <li>ADMIN – acessa a frota de todas as empresas;</li>
 *     <li>FLEET_MANAGER – acessa apenas a frota da empresa à qual está vinculado.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class FleetAccessService {

    private final IamApi iamApi;
    private final CompanyRepository companyRepository;

    /**
     * Recupera a empresa à qual as operações do usuário autenticado são restritas.
     *
     * @return empresa do gestor de frota, ou {@code null} para o administrador
     * @throws AccessDeniedException se o usuário não for ADMIN nem um gestor vinculado a uma empresa
     */
    public UUID currentCompanyId() {
        var user = iamApi.currentUser();
        if (user.role() == UserRole.ADMIN) {
            return null;
        }
        if (user.role() != UserRole.FLEET_MANAGER || user.companyUuid() == null) {
            throw new AccessDeniedException("O acesso à frota exige um administrador ou um gestor vinculado a uma empresa.");
        }
        return user.companyUuid();
    }

    /**
     * Recupera uma empresa validando o acesso do usuário autenticado.
     *
     * @param companyId identificador da empresa
     * @return empresa encontrada
     * @throws AccessDeniedException     se o gestor tentar acessar outra empresa
     * @throws BusinessException         se o identificador não for informado
     * @throws ResourceNotFoundException se a empresa não existir ou estiver excluída
     */
    @Transactional(readOnly = true)
    public Company requireCompany(UUID companyId) {
        UUID currentCompanyId = currentCompanyId();
        if (currentCompanyId != null && !currentCompanyId.equals(companyId)) {
            throw new AccessDeniedException("Não é possível acessar a frota de outra empresa.");
        }
        if (companyId == null) {
            throw new BusinessException("O identificador da empresa é obrigatório.");
        }
        return companyRepository.findByIdAndIsDeletedFalse(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com o identificador: " + companyId));
    }

    /**
     * Define a empresa proprietária de um novo cadastro da frota.
     *
     * <p>Para o gestor de frota, é sempre a própria empresa, ignorando a informada. Para o
     * administrador, é a empresa informada.</p>
     *
     * @param requestedCompanyId empresa informada na requisição
     * @return empresa proprietária do cadastro
     * @see #requireCompany(UUID)
     */
    @Transactional(readOnly = true)
    public Company resolveCompany(UUID requestedCompanyId) {
        UUID currentCompanyId = currentCompanyId();
        return requireCompany(currentCompanyId == null ? requestedCompanyId : currentCompanyId);
    }
}
