package br.com.passage.api.route.internal.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.dto.RouteResponse;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class FindRouteService {
    
    private final RouteRepository routeRepository;

    @Transactional(readOnly = true)
    public RouteResponse execute(UUID id){
        Route route = routeRepository.findById(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new BusinessException("Rota não encontrada com o identificador: " + id));
        return RouteResponse.from(route);
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> executeAll(){
        return routeRepository.findAll().stream()
                .filter(route -> !route.isDeleted())
                .map(RouteResponse::from)
                .toList();
    }
}
