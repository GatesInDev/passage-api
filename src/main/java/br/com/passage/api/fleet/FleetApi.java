package br.com.passage.api.fleet;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import br.com.passage.api.fleet.internal.repository.SeatRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * API pública do módulo de frota.
 *
 * <p>Não aplica o escopo do usuário logado: quem chama aplica a própria regra de acesso.</p>
 */
@Component
@RequiredArgsConstructor
public class FleetApi {

    private final BusRepository busRepository;
    private final DriverRepository driverRepository;
    private final SeatRepository seatRepository;

    /** @throws ResourceNotFoundException se o ônibus não existir ou estiver excluído, ou a empresa dele estiver excluída */
    @Transactional(readOnly = true)
    public BusInfo findBus(UUID busId) {
        Bus bus = busRepository.findVisibleById(busId, null)
                .orElseThrow(() -> new ResourceNotFoundException("Ônibus não encontrado com o identificador: " + busId));
        return new BusInfo(bus.getId(), bus.getCompany().getId(), bus.isActive(), bus.getCapacity());
    }

    /** @throws ResourceNotFoundException se o motorista não existir ou estiver excluído, ou a empresa dele estiver excluída */
    @Transactional(readOnly = true)
    public DriverInfo findDriver(UUID driverId) {
        Driver driver = driverRepository.findVisibleById(driverId, null)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado com o identificador: " + driverId));
        return new DriverInfo(driver.getId(), driver.getCompany().getId(), driver.isActive());
    }

    /** Indica se o assento existe, não foi excluído e pertence ao ônibus informado. */
    @Transactional(readOnly = true)
    public boolean seatBelongsToBus(UUID seatId, UUID busId) {
        return seatRepository.findByIdAndBus_IdAndIsDeletedFalse(seatId, busId).isPresent();
    }
}
