package br.com.passage.api.integration.internal.dto;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import br.com.passage.api.integration.internal.domain.enums.FiscalDocumentStatus;
import br.com.passage.api.integration.internal.domain.enums.FiscalEnvironment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Dados de um BP-e (Bilhete de Passagem Eletrônico)")
public record FiscalDocumentResponse(

        @Schema(description = "Identificador público (UUID) do documento",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "Data e hora de criação (UTC)")
        Instant createdAt,

        @Schema(description = "Data e hora da última alteração (UTC)")
        Instant updatedAt,

        @Schema(description = "Chave de acesso de 44 dígitos; nula até a autorização",
                example = "42250112345678000199630010000000011234567890")
        String accessKey,

        @Schema(description = "Modelo do documento; o BP-e é sempre o 63",
                example = "63")
        String model,

        @Schema(description = "Série do documento", example = "1")
        Integer series,

        @Schema(description = "Número sequencial dentro da série", example = "1")
        Long number,

        @Schema(description = "Protocolo de autorização da SEFAZ",
                example = "142250000123456")
        String protocol,

        @Schema(description = "Situação perante a SEFAZ",
                example = "AUTHORIZED")
        FiscalDocumentStatus status,

        @Schema(description = "Ambiente de emissão", example = "HOMOLOGATION")
        FiscalEnvironment environment,

        @Schema(description = "Passagem que originou o documento")
        UUID ticketId,

        @Schema(description = "Empresa emitente")
        UUID companyUuid,

        @Schema(description = "Nome do passageiro", example = "João da Silva")
        String passengerName,

        @Schema(description = "CPF do passageiro, sem pontuação", example = "12345678909")
        String passengerDocument,

        @Schema(description = "Valor total, em reais", example = "189.90")
        BigDecimal amount,

        @Schema(description = "Data e hora da autorização (UTC)")
        Instant authorizedAt,

        @Schema(description = "Data e hora do cancelamento (UTC)")
        Instant canceledAt,

        @Schema(description = "Motivo da recusa da SEFAZ")
        String rejectionReason
) {

    /** Converte a entidade na resposta da API. */
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
