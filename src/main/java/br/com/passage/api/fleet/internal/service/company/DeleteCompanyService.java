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
 * Exclui logicamente uma empresa; restrito ao ADMIN.
 *
 * <p>A frota não é excluída junto, mas deixa de ser visível com a empresa excluída.</p>
 */
@Service
@RequiredArgsConstructor
public class DeleteCompanyService {

    private final CompanyRepository companyRepository;
    private final IamApi iamApi;

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
