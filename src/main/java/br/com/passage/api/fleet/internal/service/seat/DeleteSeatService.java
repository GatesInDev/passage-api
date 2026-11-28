package br.com.passage.api.fleet.internal.service.seat;

import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Exclui logicamente um assento; o número e a posição continuam reservados. */
@Service
@RequiredArgsConstructor
public class DeleteSeatService {

    private final SeatRepository seatRepository;
    private final FindSeatService findSeatService;

    @Transactional
    public void execute(UUID busId, UUID id) {
        Seat seat = findSeatService.findEntity(busId, id);
        seat.setDeleted(true);
        seat.setActive(false);
        seatRepository.save(seat);
    }
}
