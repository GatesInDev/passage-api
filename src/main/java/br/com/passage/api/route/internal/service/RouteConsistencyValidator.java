package br.com.passage.api.route.internal.service;

import org.springframework.stereotype.Service;

import br.com.passage.api.route.internal.domain.enums.RouteScope;
import br.com.passage.api.shared.domain.exceptions.BusinessException;

/**
 * Valida a coerência entre origem, destino e abrangência de uma rota.
 *
 * <p>Cidades são comparadas sem diferenciar maiúsculas e espaços nas pontas.
 * Rotas internacionais não são validadas, pois só a UF é registrada.</p>
 */
@Service
public class RouteConsistencyValidator {

    public void validate(String originCity, String originState, String destinationCity, String destinationState, RouteScope scope) {
        boolean sameState = originState.equalsIgnoreCase(destinationState);
        boolean sameCity = originCity.trim().equalsIgnoreCase(destinationCity.trim());

        if (sameCity && sameState) {
            throw new BusinessException("A origem e o destino da rota não podem ser iguais.");
        }

        if ((scope == RouteScope.INTRASTATE || scope == RouteScope.METROPOLITAN) && !sameState) {
            throw new BusinessException("Rotas estaduais e metropolitanas devem começar e terminar no mesmo estado.");
        }

        if (scope == RouteScope.INTERSTATE && sameState) {
            throw new BusinessException("Rotas interestaduais devem ligar estados diferentes.");
        }
    }
}
