package br.com.passage.api.fleet.internal.dto.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa os dados de um motorista retornados pela API.
 */
@Schema(description = "Dados de um motorista")
public record DriverResponse(
        @Schema(description = "Identificador público (UUID) do motorista", example = "c943e4df-618e-4194-8be6-8a43b4d83e17")
        UUID id,
        @Schema(description = "Data e hora de criação (UTC)", example = "2026-08-13T19:20:15Z")
        Instant createdAt,
        @Schema(description = "Data e hora da última alteração (UTC)", example = "2026-08-13T19:25:00Z")
        Instant updatedAt,
        @Schema(description = "Se o motorista está ativo", example = "true")
        boolean isActive,
        @Schema(description = "Se o motorista foi excluído logicamente", example = "false")
        boolean isDeleted,
        @Schema(description = "Empresa do motorista", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID companyUuid,
        @Schema(description = "Nome completo", example = "João Pereira")
        String name,
        @Schema(description = "Número da CNH", example = "02650306461")
        String licenseNumber,
        @Schema(description = "Categoria da CNH", example = "D")
        String licenseCategory
) {
    /**
     * Converte o motorista persistido em sua representação de resposta.
     *
     * @param driver motorista de origem
     * @return representação de resposta correspondente
     */
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
