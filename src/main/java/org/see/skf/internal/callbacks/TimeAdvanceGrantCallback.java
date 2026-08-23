package org.see.skf.internal.callbacks;

import hla.rti1516_2025.time.HLAinteger64Time;

final class TimeAdvanceGrantCallback extends AbstractCallback<HLAinteger64Time> {

    TimeAdvanceGrantCallback(HLAinteger64Time targetLogicalTime) {
        super(targetLogicalTime, 1);
    }

    /*
    @Override
    protected FutureTask<HLAinteger64Time> createTask() {
        return new FutureTask<>(() -> {
            getLatch().await();
            return getOutcome();
        });
    }
     */
}
