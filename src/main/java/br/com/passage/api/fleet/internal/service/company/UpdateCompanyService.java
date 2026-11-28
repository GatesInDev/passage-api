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

/**
 * Serviço responsável pela atualização de empresas.
 *
 * <p>Permite alterar os dados cadastrais da empresa. O administrador pode atualizar qualquer
 * empresa e o gestor de frota apenas a própria.</p>
 */
@Service
@AllArgsConstructor
public class UpdateCompanyService
{
    private final CompanyRepository companyRepository;
    private final IamApi iamApi;

    /**
     * Atualiza uma empresa.
     *
     * @param id      identificador da empresa
     * @param request novos dados da empresa
     * @return empresa atualizada
     * @throws AccessDeniedException     se o usuário não puder alterar a empresa
     * @throws ResourceNotFoundException se a empresa não existir ou estiver excluída
     * @throws BusinessException         se o CNPJ já estiver em uso por outra empresa
     */
    @Transactional
    public CompanyResponse execute(UUID id, UpdateCompanyRequest request)
    {
        var currentUser = iamApi.currentUser();
        if (currentUser.role() != UserRole.ADMIN
                && (currentUser.role() != UserRole.FLEET_MANAGER || !id.equals(currentUser.companyUuid()))) {
            throw new AccessDeniedException("Você não tem permissão para atualizar esta empresa.");
        }

        Company savedCompany = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com o identificador: " + id));

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
