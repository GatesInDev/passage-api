package br.com.passage.api.fleet.internal.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Representação detalhada dos dados de resposta de uma empresa transportadora")
public record CompanyResponse(

        @Schema(
                description = "Identificador único (UUID) público da empresa",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID id,

        @Schema(
                description = "Data e hora de criação do registro (UTC)",
                example = "2026-08-13T19:20:15Z"
        )
        Instant createdAt,

        @Schema(
                description = "Data e hora da última atualização do registro (UTC)",
                example = "2026-08-13T19:25:00Z"
        )
        Instant updatedAt,

        @Schema(
                description = "Indica se a empresa está ativa para operação no sistema",
                example = "true"
        )
        boolean isActive,

        @Schema(
                description = "Indica se o registro passou por exclusão lógica (soft delete)",
                example = "false"
        )
        boolean isDeleted,

        @Schema(
                description = "Razão Social da empresa",
                example = "Viação Expressa Santa Catarina S.A."
        )
        String corporateName,

        @Schema(
                description = "Nome Fantasia da empresa",
                example = "Expressa Transportes"
        )
        String tradeName,

        @Schema(
                description = "CNPJ da empresa (somente números ou formatado)",
                example = "12345678000195"
        )
        String documentId,

        @Schema(
                description = "Inscrição Estadual (IE) da empresa",
                example = "251234567"
        )
        String stateRegistration,

        @Schema(
                description = "Órgão regulador ou concedente cadastrado (ex: DETER, ARTESP, ANTT)",
                example = "DETER-SC"
        )
        String stateRegulatoryAgency
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(), company.getCreatedAt(), company.getUpdatedAt(),
                company.isActive(), company.isDeleted(), company.getCorporateName(),
                company.getTradeName(), company.getDocumentId(),
                company.getStateRegistration(), company.getStateRegulatoryAgency()
        );
    }
}
