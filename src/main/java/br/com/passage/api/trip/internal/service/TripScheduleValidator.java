package br.com.passage.api.trip.internal.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripScheduleValidator{

    private final TripRepository tripRepository;

    public void validate(UUID busId, UUID driverId, Instant departureTime, Instant arrivalTime, UUID excludedTripId) {

        if (tripRepository.existsBusConflict(busId, departureTime, arrivalTime, excludedTripId)) {
            throw new BusinessException("O ônibus informado já possui uma viagem agendada nesse período.");
        }
        if (tripRepository.existsDriverConflict(driverId, departureTime, arrivalTime, excludedTripId)) {
            throw new BusinessException("O motorista informado já possui uma viagem agendada nesse período.");
        }
    }
}