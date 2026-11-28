package br.com.passage.api.ticket;

import java.util.UUID;

/**
 * Evento publicado quando uma passagem é vendida.
 *
 * <p>Carrega só identificadores: quem consome busca os dados atuais pela {@link TicketApi}.
 * Faz parte do contrato público do módulo, então mudar os campos afeta quem escuta o evento.</p>
 */
public record TicketSold(UUID ticketId, UUID companyUuid) {}
