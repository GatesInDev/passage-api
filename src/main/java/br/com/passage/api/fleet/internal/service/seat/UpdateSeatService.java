package br.com.passage.api.fleet.internal.service.seat;

import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.dto.seat.UpdateSeatRequest;
import br.com.passage.api.fleet.internal.dto.seat.SeatResponse;
import br.com.passage.api.fleet.internal.repository.SeatRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela atualização de assentos.
 *
 * <p>Permite alterar o número, a posição, as características e a situação do assento. O número
 * e a posição continuam únicos no ônibus.</p>
 */
@Service
@RequiredArgsConstructor
public class UpdateSeatService {

    private final SeatRepository seatRepository;
    private final FindSeatService findSeatService;

    /**
     * Atualiza um assento de um ônibus.
     *
     * @param busId   identificador do ônibus
     * @param id      identificador do assento
     * @param request novos dados do assento
     * @return assento atualizado
     * @throws ResourceNotFoundException se o ônibus ou o assento não forem encontrados
     * @throws BusinessException         se o número ou a posição já estiverem em uso no ônibus
     */
    @Transactional
    public SeatResponse execute(UUID busId, UUID id, UpdateSeatRequest request) {
        Seat seat = findSeatService.findEntity(busId, id);
        if (seatRepository.existsConflictingSeat(busId, request.seatNumber(), request.row(), request.column(), id)) {
            throw new BusinessException("O número ou a posição do assento já está em uso neste ônibus.");
        }
        seat.setSeatNumber(request.seatNumber());
        seat.setRow(request.row());
        seat.setColumn(request.column());
        seat.setPosition(request.position());
        seat.setQuality(request.quality());
        seat.setAccessible(request.isAccessible());
        seat.setActive(request.isActive());
        return SeatResponse.from(seatRepository.save(seat));
    }
}
