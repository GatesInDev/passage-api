package br.com.passage.api.fleet.internal.service.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.dto.driver.DriverResponse;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável pela consulta de motoristas.
 *
 * <p>Motoristas excluídos e motoristas de empresas excluídas não são retornados.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link FleetAccessService}: o administrador acessa
 * todas as empresas e o gestor de frota apenas a própria.</p>
 */
@Service
@RequiredArgsConstructor
public class FindDriverService {

    private final DriverRepository driverRepository;
    private final FleetAccessService fleetAccessService;

    /**
     * Busca a entidade de um motorista visível ao usuário autenticado.
     *
     * @param id identificador do motorista
     * @return motorista encontrado
     * @throws ResourceNotFoundException se o motorista não existir, estiver excluído ou pertencer a outra empresa
     */
    @Transactional(readOnly = true)
    public Driver findEntity(UUID id) {
        UUID companyId = fleetAccessService.currentCompanyId();
        return driverRepository.findVisibleById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado com o identificador: " + id));
    }

    /**
     * Busca um motorista visível ao usuário autenticado.
     *
     * @param id identificador do motorista
     * @return motorista encontrado
     * @see #findEntity(UUID)
     */
    @Transactional(readOnly = true)
    public DriverResponse execute(UUID id) {
        return DriverResponse.from(findEntity(id));
    }

    /**
     * Lista os motoristas visíveis ao usuário autenticado.
     *
     * @return motoristas encontrados
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> executeAll() {
        UUID companyId = fleetAccessService.currentCompanyId();
        return driverRepository.findAllVisible(companyId).stream()
                .map(DriverResponse::from)
                .toList();
    }
}
