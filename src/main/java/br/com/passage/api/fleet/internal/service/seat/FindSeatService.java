package br.com.passage.api.fleet.internal.service.seat;

import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.dto.seat.SeatResponse;
import br.com.passage.api.fleet.internal.repository.SeatRepository;
import br.com.passage.api.fleet.internal.service.bus.FindBusService;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável pela consulta de assentos.
 *
 * <p>Os assentos são consultados a partir de um ônibus visível ao usuário autenticado.
 * Assentos excluídos não são retornados.</p>
 */
@Service
@RequiredArgsConstructor
public class FindSeatService {

    private final SeatRepository seatRepository;
    private final FindBusService findBusService;

    /**
     * Busca a entidade de um assento de um ônibus visível ao usuário autenticado.
     *
     * @param busId identificador do ônibus
     * @param id    identificador do assento
     * @return assento encontrado
     * @throws ResourceNotFoundException se o ônibus não for visível ao usuário, ou se o assento não
     *                                   existir, estiver excluído ou pertencer a outro ônibus
     */
    @Transactional(readOnly = true)
    public Seat findEntity(UUID busId, UUID id) {
        findBusService.findEntity(busId);
        return seatRepository.findByIdAndBus_IdAndIsDeletedFalse(id, busId)
                .orElseThrow(() -> new ResourceNotFoundException("Assento não encontrado com o identificador: " + id));
    }

    /**
     * Busca um assento de um ônibus.
     *
     * @param busId identificador do ônibus
     * @param id    identificador do assento
     * @return assento encontrado
     * @see #findEntity(UUID, UUID)
     */
    @Transactional(readOnly = true)
    public SeatResponse execute(UUID busId, UUID id) {
        return SeatResponse.from(findEntity(busId, id));
    }

    /**
     * Lista os assentos de um ônibus.
     *
     * @param busId identificador do ônibus
     * @return assentos encontrados
     * @throws ResourceNotFoundException se o ônibus não existir, estiver excluído ou pertencer a outra empresa
     */
    @Transactional(readOnly = true)
    public List<SeatResponse> executeAll(UUID busId) {
        findBusService.findEntity(busId);
        return seatRepository.findAllByBus_IdAndIsDeletedFalse(busId).stream()
                .map(SeatResponse::from).toList();
    }
}
