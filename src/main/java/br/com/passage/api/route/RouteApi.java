package br.com.passage.api.route;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * API pública do módulo de rotas.
 *
 * <p>Não aplica o escopo do usuário logado: quem chama aplica a própria regra de acesso.</p>
 */
@Component
@RequiredArgsConstructor
public class RouteApi {

    private final RouteRepository routeRepository;

    /**
     * Retorna a empresa de uma rota disponível para receber viagens.
     *
     * @throws ResourceNotFoundException se a rota não existir ou estiver excluída
     * @throws BusinessException         se a rota estiver inativa
     */
    public UUID findCompanyUuid(UUID routeId){

        Route route = routeRepository.findVisibleById(routeId, null)
            .orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada com o identificador: " + routeId));

        if (!route.isActive()) {
            throw new BusinessException("A rota informada está inativa.");
        }

        return route.getCompanyUuid();
    }
}
