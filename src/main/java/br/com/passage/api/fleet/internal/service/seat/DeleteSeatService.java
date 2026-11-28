package br.com.passage.api.fleet.internal.service.seat;

import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela exclusão de assentos.
 *
 * <p>A exclusão é lógica: o assento é marcado como excluído e inativo, deixa de contar na
 * capacidade do ônibus e o número e a posição continuam reservados.</p>
 */
@Service
@RequiredArgsConstructor
public class DeleteSeatService {

    private final SeatRepository seatRepository;
    private final FindSeatService findSeatService;

    /**
     * Exclui logicamente um assento de um ônibus.
     *
     * @param busId identificador do ônibus
     * @param id    identificador do assento
     * @throws ResourceNotFoundException se o ônibus ou o assento não forem encontrados
     */
    @Transactional
    public void execute(UUID busId, UUID id) {
        Seat seat = findSeatService.findEntity(busId, id);
        seat.setDeleted(true);
        seat.setActive(false);
        seatRepository.save(seat);
    }
}
