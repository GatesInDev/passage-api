package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.passage.api.fleet.BusInfo;
import br.com.passage.api.fleet.DriverInfo;
import br.com.passage.api.fleet.FleetApi;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripFleetValidator {

    private final FleetApi fleetApi;

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