package br.com.passage.api.integration.internal.gateway;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;

/** Comunicação com a SEFAZ; interface para trocar o emissor simulado pelo real sem afetar o módulo. */
public interface SefazGateway {

    /**
     * Envia o documento para autorização.
     *
     * @return a autorização ou a recusa com o motivo
     */
    SefazResponse authorize(FiscalDocument document);
}
