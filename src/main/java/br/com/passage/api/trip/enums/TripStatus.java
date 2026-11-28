package br.com.passage.api.trip.enums;

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
    CANCELED;

    /**
     * Indica se a viagem pode passar da situação atual para a informada.
     *
     * <p>Permanecer na mesma situação é sempre permitido. As situações
     * {@link #COMPLETED} e {@link #CANCELED} são finais.</p>
     *
     * @param next situação de destino
     * @return {@code true} se a transição for permitida
     */
    public boolean canTransitionTo(TripStatus next) {
        if (this == next) {
            return true;
        }
        return switch (this) {
            case SCHEDULED -> next == BOARDING || next == CANCELED;
            case BOARDING -> next == IN_PROGRESS || next == CANCELED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELED;
            case COMPLETED, CANCELED -> false;
        };
    }
}
