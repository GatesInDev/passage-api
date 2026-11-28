package br.com.passage.api.route.internal.domain.enums;

/**
 * Representa o escopo da rota.
 *
 * <p>O escopo da rota indica a abrangência geográfica da rota, podendo ser:</p>
 *
 * <ul>
 *     <li>{@link #INTERSTATE} - Rota que atravessa diferentes estados.</li>
 *     <li>{@link #INTRASTATE} - Rota que ocorre dentro de um único estado.</li>
 *     <li>{@link #INTERNATIONAL} - Rota que atravessa fronteiras internacionais.</li>
 *     <li>{@link #METROPOLITAN} - Rota que ocorre dentro de uma área metropolitana.</li>
 * </ul>
 */
public enum RouteScope {

    /**
     * Rota que atravessa diferentes estados.
     */
    INTERSTATE,

    /**
     * Rota que ocorre dentro de um único estado.
     */
    INTRASTATE,

    /**
     * Rota que atravessa fronteiras internacionais.
     */
    INTERNATIONAL,

    /**
     * Rota que ocorre dentro de uma área metropolitana.
     */
    METROPOLITAN

}
