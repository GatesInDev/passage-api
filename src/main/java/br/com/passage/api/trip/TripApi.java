package br.com.passage.api.trip;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

/**
 * API pública do módulo de viagens.
 *
 * <p>Ponto de acesso para que outros módulos consultem informações das
 * viagens sem depender das classes internas do módulo.</p>
 */
@Component
@RequiredArgsConstructor
public class TripApi {

    private final TripRepository tripRepository;

    /**
     * Recupera uma viagem pelo identificador.
     *
     * <p>A consulta não aplica o escopo do usuário autenticado: o módulo que chama é
     * responsável por validar o acesso conforme a própria regra.</p>
     *
     * @param tripId identificador da viagem
     * @return dados da viagem
     * @throws ResourceNotFoundException se a viagem não existir ou estiver excluída
     */
    public TripInfo findTrip(UUID tripId) {
        return tripRepository.findVisibleById(tripId, null)
                .map(trip -> new TripInfo(
                        trip.getId(), trip.getCompanyUuid(), trip.getBusId(),
                        trip.getDepartureTime(), trip.getPrice(), trip.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Viagem não encontrada com o identificador: " + tripId));
    }
}
