package org.see.skf.internal.callbacks;

import hla.rti1516_2025.time.HLAinteger64Time;

import java.util.concurrent.FutureTask;

final class TimeAdvanceGrantCallback extends AbstractCallback<HLAinteger64Time> {

    TimeAdvanceGrantCallback(HLAinteger64Time targetLogicalTime) {
        super(targetLogicalTime);
    }

    @Override
    protected FutureTask<HLAinteger64Time> createTask() {
        return new FutureTask<>(() -> {
            while (true) {
                HLAinteger64Time outcome = getOutcome();

                if (outcome != null) {
                    return outcome;
                }
            }
        });
    }
}
