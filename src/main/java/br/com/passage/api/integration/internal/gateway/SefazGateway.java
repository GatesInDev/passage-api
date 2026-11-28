package br.com.passage.api.integration.internal.gateway;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;

/**
 * Canal de comunicação com a SEFAZ.
 *
 * <p>Existe como interface para que a troca do emissor simulado por uma
 * integração real não afete o restante do módulo.</p>
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
