package br.com.passage.api.trip;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

/**
 * API pública do módulo de viagens.
 *
 * <p>Não aplica o escopo do usuário logado: quem chama aplica a própria regra de acesso.</p>
 */
@Component
@RequiredArgsConstructor
public class TripApi {

    private final TripRepository tripRepository;

    /** @throws ResourceNotFoundException se a viagem não existir ou estiver excluída */
    public TripInfo findTrip(UUID tripId) {
        return tripRepository.findVisibleById(tripId, null)
                .map(trip -> new TripInfo(
                        trip.getId(), trip.getCompanyUuid(), trip.getBusId(),
                        trip.getDepartureTime(), trip.getPrice(), trip.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Viagem não encontrada com o identificador: " + tripId));
    }
}
