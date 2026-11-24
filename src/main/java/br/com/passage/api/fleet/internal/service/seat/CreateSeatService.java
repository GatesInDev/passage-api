package br.com.passage.api.fleet.internal.service.seat;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.dto.seat.CreateSeatRequest;
import br.com.passage.api.fleet.internal.dto.seat.SeatResponse;
import br.com.passage.api.fleet.internal.repository.SeatRepository;
import br.com.passage.api.fleet.internal.service.bus.FindBusService;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateSeatService {

    private final SeatRepository seatRepository;
    private final FindBusService findBusService;

    @Transactional
    public SeatResponse execute(UUID busId, CreateSeatRequest request) {
        Bus bus = findBusService.findEntity(busId);
        if (seatRepository.existsConflictingSeat(busId, request.seatNumber(), request.row(), request.column(), null)) {
            throw new BusinessException("Seat number or position is already in use on this bus.");
        }
        Seat seat = Seat.builder()
                .bus(bus)
                .seatNumber(request.seatNumber())
                .row(request.row())
                .column(request.column())
                .position(request.position())
                .quality(request.quality())
                .isAccessible(request.isAccessible())
                .isActive(true)
                .build();
        bus.getSeats().add(seat);
        return SeatResponse.from(seatRepository.save(seat));
    }
}
