package org.openbaton.catalogue.mano.common.monitoring;

/**
 * Created by mob on 26.10.15.
 */
public enum PerceivedSeverity {
    CRITICAL(4),
    MAJOR(3),
    MINOR(2),
    WARNING(1),
    INDETERMINATE (0);

    private int severityLevel;

    PerceivedSeverity(int severityLevel) {
        this.severityLevel = severityLevel;
    }
}
