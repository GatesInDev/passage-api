package br.com.passage.api.ticket;

import java.util.UUID;

/**
 * Publicado quando uma passagem é vendida.
 *
 * <p>Carrega apenas identificadores: os dados da passagem são obtidos
 * pela {@link TicketApi} no momento do consumo, de modo que o documento
 * fiscal reflita o estado atual do bilhete.</p>
 *
 * <p>É o contrato público do módulo: alterar seus campos afeta quem
 * escuta o evento.</p>
 */
public record TicketSold(UUID ticketId, UUID companyUuid) {}
