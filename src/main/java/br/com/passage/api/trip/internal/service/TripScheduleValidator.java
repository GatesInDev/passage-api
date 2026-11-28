package br.com.passage.api.trip.internal.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

/**
 * Validador da agenda do ônibus e do motorista de uma viagem.
 *
 * <p>O mesmo ônibus ou motorista não pode estar em duas viagens que se sobrepõem no mesmo
 * período.</p>
 */
@Service
@RequiredArgsConstructor
public class TripScheduleValidator{

    private final TripRepository tripRepository;

    /**
     * Valida se o ônibus e o motorista estão livres no período da viagem.
     *
     * @param busId          identificador do ônibus
     * @param driverId       identificador do motorista
     * @param departureTime  data e hora de partida
     * @param arrivalTime    data e hora de chegada
     * @param excludedTripId viagem desconsiderada na verificação, ou {@code null} no cadastro
     * @throws BusinessException se o ônibus ou o motorista já possuírem viagem no período
     */
    public void validate(UUID busId, UUID driverId, Instant departureTime, Instant arrivalTime, UUID excludedTripId) {

        if (tripRepository.existsBusConflict(busId, departureTime, arrivalTime, excludedTripId)) {
            throw new BusinessException("O ônibus informado já possui uma viagem agendada nesse período.");
        }
        if (tripRepository.existsDriverConflict(driverId, departureTime, arrivalTime, excludedTripId)) {
            throw new BusinessException("O motorista informado já possui uma viagem agendada nesse período.");
        }
    }
}