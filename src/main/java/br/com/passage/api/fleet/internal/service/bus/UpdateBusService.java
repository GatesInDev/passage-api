package br.com.passage.api.fleet.internal.service.bus;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.dto.bus.UpdateBusRequest;
import br.com.passage.api.fleet.internal.dto.bus.BusResponse;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Atualiza um ônibus visível ao usuário logado; a empresa não muda. */
@Service
@RequiredArgsConstructor
public class UpdateBusService {

    private final BusRepository busRepository;
    private final FindBusService findBusService;

    @Transactional
    public BusResponse execute(UUID id, UpdateBusRequest request) {
        Bus bus = findBusService.findEntity(id);
        if (busRepository.existsByLicensePlateAndIdNot(request.licensePlate(), id)) {
            throw new BusinessException("Já existe um ônibus cadastrado com a placa " + request.licensePlate());
        }
        bus.setCode(request.code());
        bus.setModel(request.model());
        bus.setManufacturer(request.manufacturer());
        bus.setLicensePlate(request.licensePlate());
        bus.setActive(request.isActive());
        return BusResponse.from(busRepository.save(bus));
    }
}
