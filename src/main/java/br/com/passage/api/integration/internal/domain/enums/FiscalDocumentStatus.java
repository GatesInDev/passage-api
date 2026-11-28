package br.com.passage.api.integration.internal.domain.enums;

/**
 * Situação do documento fiscal perante a SEFAZ.
 */
public enum FiscalDocumentStatus {

    /** Gerado, aguardando a resposta da SEFAZ. */
    PENDING,

    /** Autorizado, com protocolo. */
    AUTHORIZED,

    /** Recusado pela SEFAZ; o motivo fica registrado. */
    REJECTED,

    /** Cancelado depois de autorizado. */
    CANCELED
}
