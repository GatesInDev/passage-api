package br.com.passage.api.fleet.internal.dto.bus;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Bus details.")
public record BusResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        boolean isActive,
        boolean isDeleted,
        UUID companyUuid,
        String code,
        String model,
        String manufacturer,
        String licensePlate,
        int capacity
) {
    public static BusResponse from(Bus bus) {
        return new BusResponse(
                bus.getId(),
                bus.getCreatedAt(),
                bus.getUpdatedAt(),
                bus.isActive(),
                bus.isDeleted(),
                bus.getCompany().getId(),
                bus.getCode(),
                bus.getModel(),
                bus.getManufacturer(),
                bus.getLicensePlate(),
                bus.getCapacity()
        );
    }
}
