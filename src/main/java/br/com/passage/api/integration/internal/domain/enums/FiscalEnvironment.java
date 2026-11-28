package br.com.passage.api.integration.internal.domain.enums;

/** Ambiente de emissão do documento fiscal. */
public enum FiscalEnvironment {

    /** Com validade fiscal. */
    PRODUCTION,

    /** Testes, sem validade fiscal. */
    HOMOLOGATION
}
