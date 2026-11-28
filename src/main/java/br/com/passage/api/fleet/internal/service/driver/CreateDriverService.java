package br.com.passage.api.fleet.internal.service.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.dto.driver.CreateDriverRequest;
import br.com.passage.api.fleet.internal.dto.driver.DriverResponse;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelo cadastro de motoristas.
 *
 * <p>O motorista é cadastrado ativo. O número da CNH deve ser único no sistema.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link FleetAccessService}: o administrador acessa
 * todas as empresas e o gestor de frota apenas a própria.</p>
 */
@Service
@RequiredArgsConstructor
public class CreateDriverService {

    private final DriverRepository driverRepository;
    private final FleetAccessService fleetAccessService;

    /**
     * Cadastra um motorista.
     *
     * @param request dados do motorista
     * @return motorista cadastrado
     * @throws BusinessException         se a CNH já estiver em uso ou a empresa não for informada
     * @throws ResourceNotFoundException se a empresa não existir ou estiver excluída
     */
    @Transactional
    public DriverResponse execute(CreateDriverRequest request) {
        Company company = fleetAccessService.resolveCompany(request.companyUuid());
        if (driverRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new BusinessException("Já existe um motorista cadastrado com a CNH " + request.licenseNumber());
        }
        Driver driver = new Driver(request.name(), request.licenseNumber(), request.licenseCategory(), company);
        return DriverResponse.from(driverRepository.save(driver));
    }
}
