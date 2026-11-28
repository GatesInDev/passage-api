package br.com.passage.api.fleet.internal.service.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.dto.driver.UpdateDriverRequest;
import br.com.passage.api.fleet.internal.dto.driver.DriverResponse;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela atualização de motoristas.
 *
 * <p>Permite alterar o nome, a CNH e a situação do motorista. A empresa à qual o motorista está
 * vinculado não é alterada.</p>
 */
@Service
@RequiredArgsConstructor
public class UpdateDriverService {

    private final DriverRepository driverRepository;
    private final FindDriverService findDriverService;

    /**
     * Atualiza um motorista visível ao usuário autenticado.
     *
     * @param id      identificador do motorista
     * @param request novos dados do motorista
     * @return motorista atualizado
     * @throws ResourceNotFoundException se o motorista não existir, estiver excluído ou pertencer a outra empresa
     * @throws BusinessException         se a CNH já estiver em uso por outro motorista
     */
    @Transactional
    public DriverResponse execute(UUID id, UpdateDriverRequest request) {
        Driver driver = findDriverService.findEntity(id);
        if (driverRepository.existsByLicenseNumberAndIdNot(request.licenseNumber(), id)) {
            throw new BusinessException("Já existe um motorista cadastrado com a CNH " + request.licenseNumber());
        }
        driver.setName(request.name());
        driver.setLicenseNumber(request.licenseNumber());
        driver.setLicenseCategory(request.licenseCategory());
        driver.setActive(request.isActive());
        return DriverResponse.from(driverRepository.save(driver));
    }
}
