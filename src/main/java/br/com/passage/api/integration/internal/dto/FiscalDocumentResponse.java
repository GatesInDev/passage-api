package br.com.passage.api.integration.internal.dto;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import br.com.passage.api.integration.internal.domain.enums.FiscalDocumentStatus;
import br.com.passage.api.integration.internal.domain.enums.FiscalEnvironment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Bilhete de Passagem Eletrônico (BP-e) emitido para uma passagem")
public record FiscalDocumentResponse(

        @Schema(description = "Identificador único (UUID) do documento",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "Data e hora de criação do registro (UTC)")
        Instant createdAt,

        @Schema(description = "Data e hora da última atualização do registro (UTC)")
        Instant updatedAt,

        @Schema(description = "Chave de acesso de 44 dígitos atribuída pela SEFAZ",
                example = "42250112345678000199630010000000011234567890")
        String accessKey,

        @Schema(description = "Modelo do documento fiscal. O BP-e é o modelo 63",
                example = "63")
        String model,

        @Schema(description = "Série do documento", example = "1")
        Integer series,

        @Schema(description = "Número sequencial dentro da série", example = "1")
        Long number,

        @Schema(description = "Protocolo de autorização devolvido pela SEFAZ",
                example = "142250000123456")
        String protocol,

        @Schema(description = "Situação do documento perante a SEFAZ",
                example = "AUTHORIZED")
        FiscalDocumentStatus status,

        @Schema(description = "Ambiente de emissão", example = "HOMOLOGATION")
        FiscalEnvironment environment,

        @Schema(description = "Identificador UUID da passagem que originou o documento")
        UUID ticketId,

        @Schema(description = "Identificador UUID da empresa emitente")
        UUID companyUuid,

        @Schema(description = "Nome do passageiro", example = "João da Silva")
        String passengerName,

        @Schema(description = "CPF do passageiro", example = "12345678909")
        String passengerDocument,

        @Schema(description = "Valor total do documento em reais", example = "189.90")
        BigDecimal amount,

        @Schema(description = "Data e hora da autorização (UTC)")
        Instant authorizedAt,

        @Schema(description = "Data e hora do cancelamento (UTC)")
        Instant canceledAt,

        @Schema(description = "Motivo informado pela SEFAZ quando o documento é recusado")
        String rejectionReason
) {

    public static FiscalDocumentResponse from(FiscalDocument document) {
        return new FiscalDocumentResponse(
                document.getId(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getAccessKey(),
                document.getModel(),
                document.getSeries(),
                document.getNumber(),
                document.getProtocol(),
                document.getStatus(),
                document.getEnvironment(),
                document.getTicketId(),
                document.getCompanyUuid(),
                document.getPassengerName(),
                document.getPassengerDocument(),
                document.getAmount(),
                document.getAuthorizedAt(),
                document.getCanceledAt(),
                document.getRejectionReason()
        );
    }
}
