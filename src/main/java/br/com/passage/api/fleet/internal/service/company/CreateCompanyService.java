package br.com.passage.api.fleet.internal.service.company;

import br.com.passage.api.fleet.internal.dto.company.CreateCompanyRequest;
import br.com.passage.api.fleet.internal.dto.company.CompanyResponse;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cadastra uma empresa; o CNPJ é único no sistema. */
@Service
@AllArgsConstructor
public class CreateCompanyService
{
    private final CompanyRepository companyRepository;

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
