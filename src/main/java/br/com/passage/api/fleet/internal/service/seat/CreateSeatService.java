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

/**
 * Serviço responsável pelo cadastro de assentos.
 *
 * <p>O assento é cadastrado ativo em um ônibus visível ao usuário autenticado. O número e a
 * posição (fileira e coluna) devem ser únicos no ônibus.</p>
 */
@Service
@RequiredArgsConstructor
public class CreateSeatService {

    private final SeatRepository seatRepository;
    private final FindBusService findBusService;

    /**
     * Cadastra um assento em um ônibus.
     *
     * @param busId   identificador do ônibus
     * @param request dados do assento
     * @return assento cadastrado
     * @throws ResourceNotFoundException se o ônibus não existir, estiver excluído ou pertencer a outra empresa
     * @throws BusinessException         se o número ou a posição já estiverem em uso no ônibus
     */
    @Transactional
    public SeatResponse execute(UUID busId, CreateSeatRequest request) {
        Bus bus = findBusService.findEntity(busId);
        if (seatRepository.existsConflictingSeat(busId, request.seatNumber(), request.row(), request.column(), null)) {
            throw new BusinessException("O número ou a posição do assento já está em uso neste ônibus.");
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
