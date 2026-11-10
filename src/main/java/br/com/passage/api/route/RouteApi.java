package br.com.passage.api.route;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class RouteApi {
    
    private final RouteRepository routeRepository;

    public UUID findCompanyUuid(UUID routeId){

        return routeRepository.findById(routeId)
            .filter(found -> !found.isDeleted())
            .map(Route::getCompanyUuid)
            .orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada com o identificador: " + routeId));
    }
}
