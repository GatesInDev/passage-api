package br.com.passage.api.integration.internal.domain.enums;

/**
 * Ambiente de emissão, conforme a legislação fiscal.
 */
public enum FiscalEnvironment {

    /**
     * Ambiente de produção: documentos com validade fiscal.
     */
    PRODUCTION,

    /**
     * Ambiente de homologação: documentos sem validade fiscal.
     */
    HOMOLOGATION
}
