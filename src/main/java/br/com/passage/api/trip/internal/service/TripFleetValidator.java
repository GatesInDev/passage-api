package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.passage.api.fleet.BusInfo;
import br.com.passage.api.fleet.DriverInfo;
import br.com.passage.api.fleet.FleetApi;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;

/**
 * Validador do ônibus e do motorista alocados em uma viagem.
 *
 * <p>Os dados são consultados no módulo de frota pela {@code FleetApi}. O ônibus e o motorista
 * devem pertencer à mesma empresa da viagem e estar ativos.</p>
 */
@Service
@RequiredArgsConstructor
public class TripFleetValidator {

    private final FleetApi fleetApi;

    /**
     * Valida o ônibus e o motorista da viagem.
     *
     * @param companyUuid empresa da viagem
     * @param busId       identificador do ônibus
     * @param driverId    identificador do motorista
     * @throws ResourceNotFoundException se o ônibus ou o motorista não existirem ou estiverem excluídos
     * @throws BusinessException         se o ônibus ou o motorista pertencerem a outra empresa ou
     *                                   estiverem inativos
     */
    public void validate(UUID companyUuid, UUID busId, UUID driverId) {
        BusInfo bus = fleetApi.findBus(busId);          
        DriverInfo driver = fleetApi.findDriver(driverId);

        if (!bus.companyUuid().equals(companyUuid)) {
            throw new BusinessException("O ônibus informado pertence a outra empresa.");
        }
        if (!bus.active()) {
            throw new BusinessException("O ônibus informado está inativo.");
        }

        if (!driver.companyUuid().equals(companyUuid)) {
            throw new BusinessException("O motorista informado pertence a outra empresa.");
        }
        if (!driver.active()) {
            throw new BusinessException("O motorista informado está inativo.");
        }
    }
}