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
 * <p>Ponto de acesso para que outros módulos consultem ônibus, motoristas e assentos sem
 * depender das classes internas do módulo.</p>
 *
 * <p>As consultas não aplicam o escopo do usuário autenticado: o módulo que chama é
 * responsável por validar o acesso conforme a própria regra.</p>
 */
@Component
@RequiredArgsConstructor
public class FleetApi {

    private final BusRepository busRepository;
    private final DriverRepository driverRepository;
    private final SeatRepository seatRepository;

    /**
     * Recupera um ônibus pelo identificador.
     *
     * @param busId identificador do ônibus
     * @return dados do ônibus
     * @throws ResourceNotFoundException se o ônibus não existir, estiver excluído ou pertencer a uma
     *                                   empresa excluída
     */
    @Transactional(readOnly = true)
    public BusInfo findBus(UUID busId) {
        Bus bus = busRepository.findVisibleById(busId, null)
                .orElseThrow(() -> new ResourceNotFoundException("Ônibus não encontrado com o identificador: " + busId));
        return new BusInfo(bus.getId(), bus.getCompany().getId(), bus.isActive(), bus.getCapacity());
    }

    /**
     * Recupera um motorista pelo identificador.
     *
     * @param driverId identificador do motorista
     * @return dados do motorista
     * @throws ResourceNotFoundException se o motorista não existir, estiver excluído ou pertencer a uma
     *                                   empresa excluída
     */
    @Transactional(readOnly = true)
    public DriverInfo findDriver(UUID driverId) {
        Driver driver = driverRepository.findVisibleById(driverId, null)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado com o identificador: " + driverId));
        return new DriverInfo(driver.getId(), driver.getCompany().getId(), driver.isActive());
    }

    /**
     * Verifica se o assento pertence ao ônibus informado.
     *
     * @param seatId identificador do assento
     * @param busId  identificador do ônibus
     * @return {@code true} se o assento existir, não estiver excluído e pertencer ao ônibus
     */
    @Transactional(readOnly = true)
    public boolean seatBelongsToBus(UUID seatId, UUID busId) {
        return seatRepository.findByIdAndBus_IdAndIsDeletedFalse(seatId, busId).isPresent();
    }
}
