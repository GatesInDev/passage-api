package br.com.passage.api.route.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela exclusão de rotas.
 *
 * <p>A exclusão é lógica: a rota é marcada como excluída e inativa e permanece no banco.</p>
 */
@Service
@RequiredArgsConstructor
public class DeleteRouteService {
    
    private final RouteRepository routeRepository;
    private final RouteAccessService routeAccessService;

    /**
     * Exclui logicamente uma rota visível ao usuário autenticado.
     *
     * @param id identificador da rota
     * @throws ResourceNotFoundException se a rota não existir, estiver excluída ou pertencer a outra empresa
     */
    @Transactional 
    public void execute(UUID id){
        Route route = routeRepository.findVisibleById(id, routeAccessService.visibleCompanyUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada com o identificador: " + id));

        route.setDeleted(true);
        route.setActive(false);
        routeRepository.save(route);

    }
}
