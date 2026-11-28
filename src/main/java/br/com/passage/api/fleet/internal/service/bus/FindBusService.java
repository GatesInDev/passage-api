package br.com.passage.api.fleet.internal.service.bus;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.dto.bus.BusResponse;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável pela consulta de ônibus.
 *
 * <p>Ônibus excluídos e ônibus de empresas excluídas não são retornados.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link FleetAccessService}: o administrador acessa
 * todas as empresas e o gestor de frota apenas a própria.</p>
 */
@Service
@RequiredArgsConstructor
public class FindBusService {

    private final BusRepository busRepository;
    private final FleetAccessService fleetAccessService;

    /**
     * Busca a entidade de um ônibus visível ao usuário autenticado.
     *
     * @param id identificador do ônibus
     * @return ônibus encontrado
     * @throws ResourceNotFoundException se o ônibus não existir, estiver excluído ou pertencer a outra empresa
     */
    @Transactional(readOnly = true)
    public Bus findEntity(UUID id) {
        UUID companyId = fleetAccessService.currentCompanyId();
        return busRepository.findVisibleById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ônibus não encontrado com o identificador: " + id));
    }

    /**
     * Busca um ônibus visível ao usuário autenticado.
     *
     * @param id identificador do ônibus
     * @return ônibus encontrado
     * @see #findEntity(UUID)
     */
    @Transactional(readOnly = true)
    public BusResponse execute(UUID id) {
        return BusResponse.from(findEntity(id));
    }

    /**
     * Lista os ônibus visíveis ao usuário autenticado.
     *
     * @return ônibus encontrados
     */
    @Transactional(readOnly = true)
    public List<BusResponse> executeAll() {
        UUID companyId = fleetAccessService.currentCompanyId();
        return busRepository.findAllVisible(companyId).stream()
                .map(BusResponse::from)
                .toList();
    }
}
