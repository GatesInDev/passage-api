package br.com.passage.api.integration.internal.domain.enums;

/**
 * Situação do documento fiscal perante a SEFAZ.
 */
public enum FiscalDocumentStatus {

    /**
     * Gerado, aguardando envio ou resposta da SEFAZ.
     */
    PENDING,

    /**
     * Autorizado: possui protocolo e pode circular.
     */
    AUTHORIZED,

    /**
     * Recusado pela SEFAZ; o motivo fica registrado.
     */
    REJECTED,

    /**
     * Cancelado por evento posterior à autorização.
     */
    CANCELED
}
