package br.com.passage.api.fleet.internal.service.company;

import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela exclusão de empresas.
 *
 * <p>A exclusão é lógica e restrita ao administrador. A empresa é marcada como excluída e
 * inativa, e os ônibus e motoristas dela deixam de ser retornados pelas consultas da frota.</p>
 */
@Service
@RequiredArgsConstructor
public class DeleteCompanyService {

    private final CompanyRepository companyRepository;
    private final IamApi iamApi;

    /**
     * Exclui logicamente uma empresa.
     *
     * @param id identificador da empresa
     * @throws AccessDeniedException     se o usuário não for administrador
     * @throws ResourceNotFoundException se a empresa não existir ou já estiver excluída
     */
    @Transactional
    public void execute(UUID id) {
        if (iamApi.currentUser().role() != UserRole.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem excluir empresas.");
        }
        Company company = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com o identificador: " + id));
        company.setDeleted(true);
        company.setActive(false);
        companyRepository.save(company);
    }
}
