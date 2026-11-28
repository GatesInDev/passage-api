package br.com.passage.api.trip.enums;

/**
 * Situação de uma viagem ao longo da operação.
 *
 * <p>Fluxo: {@code SCHEDULED → BOARDING → IN_PROGRESS → COMPLETED}, com cancelamento
 * possível até a conclusão. {@link #COMPLETED} e {@link #CANCELED} são finais.</p>
 */
public enum TripStatus {

    /** Agendada, ainda não iniciada. */
    SCHEDULED,

    /** Embarque em andamento. */
    BOARDING,

    /** Em viagem. */
    IN_PROGRESS,

    /** Concluída. */
    COMPLETED,

    /** Cancelada; libera o ônibus e o motorista. */
    CANCELED;

    /**
     * Indica se a viagem pode passar da situação atual para {@code next}.
     *
     * Permanecer na mesma situação é sempre permitido.
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

    /** Concluída ou cancelada: a viagem não aceita mais alterações. */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELED;
    }
}
