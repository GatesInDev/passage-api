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

/** Consulta ônibus no escopo do usuário logado (ver {@link FleetAccessService}). */
@Service
@RequiredArgsConstructor
public class FindBusService {

    private final BusRepository busRepository;
    private final FleetAccessService fleetAccessService;

    /** @throws ResourceNotFoundException se o ônibus não existir, estiver excluído ou for de outra empresa */
    @Transactional(readOnly = true)
    public Bus findEntity(UUID id) {
        UUID companyId = fleetAccessService.currentCompanyId();
        return busRepository.findVisibleById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found: " + id));
    }

    @Transactional(readOnly = true)
    public BusResponse execute(UUID id) {
        return BusResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<BusResponse> executeAll() {
        UUID companyId = fleetAccessService.currentCompanyId();
        return busRepository.findAllVisible(companyId).stream()
                .map(BusResponse::from)
                .toList();
    }
}
