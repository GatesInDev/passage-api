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

@Service
@RequiredArgsConstructor
public class FindSeatService {

    private final SeatRepository seatRepository;
    private final FindBusService findBusService;

    @Transactional(readOnly = true)
    public Seat findEntity(UUID busId, UUID id) {
        findBusService.findEntity(busId);
        return seatRepository.findByIdAndBus_IdAndIsDeletedFalse(id, busId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + id));
    }

    @Transactional(readOnly = true)
    public SeatResponse execute(UUID busId, UUID id) {
        return SeatResponse.from(findEntity(busId, id));
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> executeAll(UUID busId) {
        findBusService.findEntity(busId);
        return seatRepository.findAllByBus_IdAndIsDeletedFalse(busId).stream()
                .map(SeatResponse::from).toList();
    }
}
