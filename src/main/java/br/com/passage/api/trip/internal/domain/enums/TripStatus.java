package br.com.passage.api.trip.internal.domain.enums;

/**
 * Representa a situação de uma viagem.
 *
 * <p>A situação indica em qual etapa do ciclo de vida a viagem se encontra,
 * podendo ser:</p>
 *
 * <ul>
 *     <li>{@link #SCHEDULED} - Viagem agendada, ainda não iniciada.</li>
 *     <li>{@link #BOARDING} - Embarque dos passageiros em andamento.</li>
 *     <li>{@link #IN_PROGRESS} - Viagem em curso.</li>
 *     <li>{@link #COMPLETED} - Viagem concluída.</li>
 *     <li>{@link #CANCELED} - Viagem cancelada.</li>
 * </ul>
 */
public enum TripStatus {

    /**
     * Viagem agendada, ainda não iniciada.
     */
    SCHEDULED,

    /**
     * Embarque dos passageiros em andamento.
     */
    BOARDING,

    /**
     * Viagem em curso.
     */
    IN_PROGRESS,

    /**
     * Viagem concluída.
     */
    COMPLETED,

    /**
     * Viagem cancelada.
     */
    CANCELED
}
