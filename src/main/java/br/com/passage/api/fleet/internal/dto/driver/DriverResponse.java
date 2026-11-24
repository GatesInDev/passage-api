package br.com.passage.api.fleet.internal.dto.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Driver details.")
public record DriverResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        boolean isActive,
        boolean isDeleted,
        UUID companyUuid,
        String name,
        String licenseNumber,
        String licenseCategory
) {
    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getCreatedAt(),
                driver.getUpdatedAt(),
                driver.isActive(),
                driver.isDeleted(),
                driver.getCompany().getId(),
                driver.getName(),
                driver.getLicenseNumber(),
                driver.getLicenseCategory()
        );
    }
}
