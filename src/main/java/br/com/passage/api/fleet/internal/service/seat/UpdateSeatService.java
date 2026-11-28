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

/** Atualiza um assento de um ônibus visível ao usuário logado. */
@Service
@RequiredArgsConstructor
public class UpdateSeatService {

    private final SeatRepository seatRepository;
    private final FindSeatService findSeatService;

    @Transactional
    public SeatResponse execute(UUID busId, UUID id, UpdateSeatRequest request) {
        Seat seat = findSeatService.findEntity(busId, id);
        if (seatRepository.existsConflictingSeat(busId, request.seatNumber(), request.row(), request.column(), id)) {
            throw new BusinessException("Seat number or position is already in use on this bus.");
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
