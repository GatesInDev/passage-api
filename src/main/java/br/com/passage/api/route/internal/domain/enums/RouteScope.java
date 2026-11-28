package br.com.passage.api.route.internal.domain.enums;

/**
 * Abrangência geográfica da rota.
 */
public enum RouteScope {

    /** Entre estados diferentes; regulada pela ANTT. */
    INTERSTATE,

    /** Dentro de um mesmo estado; regulada pelo órgão estadual. */
    INTRASTATE,

    /** Cruza a fronteira do país; regulada pela ANTT. */
    INTERNATIONAL,

    /** Dentro de uma região metropolitana, no mesmo estado. */
    METROPOLITAN

}
