package org.see.skf.callbacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.FutureTask;

final class NameReservationCallback extends AbstractBiCallback<String, Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(NameReservationCallback.class);

    NameReservationCallback(String objectInstanceName, Boolean outcome) {
        super(objectInstanceName, outcome);
    }

    @Override
    protected FutureTask<Boolean> createTask() {
        return new FutureTask<>(() -> {
            while (true) {
                Boolean reservationOutcome = getOutcome();

                if (reservationOutcome != null) {
                    String outcome = reservationOutcome ? "SUCCESS" : "FAILURE";
                    logger.debug("Name reservation process for <{}> complete. Outcome: {}", getTarget(), outcome);
                    return reservationOutcome;
                }
            }
        });
    }
}
