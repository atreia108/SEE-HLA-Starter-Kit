package org.see.skf.internal;

public enum SRFOMSynchronizationPoint {
    INITIALIZATION_STARTED ("initialization_started"),
    INITIALIZATION_COMPLETED ("initialization_completed"),
    OBJECTS_DISCOVERED ("objects_discovered"),
    MTR_RUN ("mtr_run"),
    MTR_FREEZE ("mtr_freeze"),
    MTR_SHUTDOWN ("mtr_shutdown"),
    MPI1 ("MPI1"),
    MPI2 ("MPI2");

    private final String label;

    SRFOMSynchronizationPoint(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public static SRFOMSynchronizationPoint query(String label) {
        for (SRFOMSynchronizationPoint syncPoint : SRFOMSynchronizationPoint.values()) {
            if (syncPoint.getLabel().equals(label)) {
                return syncPoint;
            }
        }

        return null;
    }
}
