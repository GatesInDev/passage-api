package br.com.passage.api.fleet.internal.service.bus;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela exclusão de ônibus.
 *
 * <p>A exclusão é lógica: o ônibus é marcado como excluído e inativo e permanece no banco.</p>
 */
@Service
@RequiredArgsConstructor
public class DeleteBusService {

    private final BusRepository busRepository;
    private final FindBusService findBusService;

    /**
     * Exclui logicamente um ônibus visível ao usuário autenticado.
     *
     * @param id identificador do ônibus
     * @throws ResourceNotFoundException se o ônibus não existir, estiver excluído ou pertencer a outra empresa
     */
    @Transactional
    public void execute(UUID id) {
        Bus bus = findBusService.findEntity(id);
        bus.setDeleted(true);
        bus.setActive(false);
        busRepository.save(bus);
    }
}
