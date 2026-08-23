package org.see.skf.internal.callbacks;

final class NameReservationCallback extends AbstractBiCallback<String, Boolean> {

    NameReservationCallback(String objectInstanceName, Boolean outcome) {
        super(objectInstanceName, outcome, 1);
    }

    /*
    @Override
    protected FutureTask<Boolean> createTask() {
        return new FutureTask<>(() -> {
            getLatch().await();
            return getOutcome();
        });
    }
     */
}
