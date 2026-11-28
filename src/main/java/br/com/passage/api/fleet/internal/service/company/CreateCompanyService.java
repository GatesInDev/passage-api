package br.com.passage.api.fleet.internal.service.company;

import br.com.passage.api.fleet.internal.dto.company.CreateCompanyRequest;
import br.com.passage.api.fleet.internal.dto.company.CompanyResponse;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelo cadastro de empresas.
 *
 * <p>A empresa é cadastrada ativa. O CNPJ deve ser único no sistema.</p>
 */
@Service
@AllArgsConstructor
public class CreateCompanyService
{
    private final CompanyRepository companyRepository;

    /**
     * Cadastra uma empresa.
     *
     * @param request dados da empresa
     * @return empresa cadastrada
     * @throws BusinessException se o CNPJ já estiver em uso
     */
    @Transactional
    public CompanyResponse execute(CreateCompanyRequest request)
    {
        if (companyRepository.existsByDocumentId(request.documentId()))
        {
            throw new BusinessException("Já existe uma empresa cadastrada com o CNPJ " + request.documentId());
        }

        Company company = Company.builder()
                .isActive(true)
                .corporateName(request.corporateName())
                .tradeName(request.tradeName())
                .documentId(request.documentId())
                .stateRegistration(request.stateRegistration())
                .stateRegulatoryAgency(request.stateRegulatoryAgency())
                .build();

        Company savedCompany = companyRepository.save(company);

        return CompanyResponse.from(savedCompany);
    }
}
