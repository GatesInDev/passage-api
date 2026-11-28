package br.com.passage.api.integration.internal.gateway;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;

/**
 * Canal de comunicação com a SEFAZ.
 *
 * <p>Nesta versão, a implementação utilizada é o {@link MockSefazGateway}.</p>
 */
public interface SefazGateway {

    /**
     * Submete o documento à autorização.
     *
     * @param document documento a autorizar
     * @return autorização concedida ou recusa com o motivo
     */
    SefazResponse authorize(FiscalDocument document);
}
