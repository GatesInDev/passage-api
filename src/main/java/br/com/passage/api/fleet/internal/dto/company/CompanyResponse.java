package br.com.passage.api.fleet.internal.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa os dados de uma empresa retornados pela API.
 */
@Schema(description = "Dados de uma empresa")
public record CompanyResponse(

        @Schema(
                description = "Identificador público (UUID) da empresa",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID id,

        @Schema(
                description = "Data e hora de criação (UTC)",
                example = "2026-08-13T19:20:15Z"
        )
        Instant createdAt,

        @Schema(
                description = "Data e hora da última alteração (UTC)",
                example = "2026-08-13T19:25:00Z"
        )
        Instant updatedAt,

        @Schema(
                description = "Se a empresa está ativa",
                example = "true"
        )
        boolean isActive,

        @Schema(
                description = "Se a empresa foi excluída logicamente",
                example = "false"
        )
        boolean isDeleted,

        @Schema(
                description = "Razão social",
                example = "Viação Expressa Santa Catarina S.A."
        )
        String corporateName,

        @Schema(
                description = "Nome fantasia",
                example = "Expressa Transportes"
        )
        String tradeName,

        @Schema(
                description = "CNPJ, sem pontuação",
                example = "12345678000195"
        )
        String documentId,

        @Schema(
                description = "Inscrição estadual, ou ISENTO",
                example = "251234567"
        )
        String stateRegistration,

        @Schema(
                description = "Órgão regulador estadual",
                example = "DETER-SC"
        )
        String stateRegulatoryAgency
) {
    /**
     * Converte a empresa persistida em sua representação de resposta.
     *
     * @param company empresa de origem
     * @return representação de resposta correspondente
     */
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(), company.getCreatedAt(), company.getUpdatedAt(),
                company.isActive(), company.isDeleted(), company.getCorporateName(),
                company.getTradeName(), company.getDocumentId(),
                company.getStateRegistration(), company.getStateRegulatoryAgency()
        );
    }
}
