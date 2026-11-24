package br.com.passage.api.fleet.internal.service.bus;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteBusService {

    private final BusRepository busRepository;
    private final FindBusService findBusService;

    @Transactional
    public void execute(UUID id) {
        Bus bus = findBusService.findEntity(id);
        bus.setDeleted(true);
        bus.setActive(false);
        busRepository.save(bus);
    }
}
