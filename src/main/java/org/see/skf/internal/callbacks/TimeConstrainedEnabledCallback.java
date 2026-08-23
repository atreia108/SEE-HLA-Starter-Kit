package org.see.skf.internal.callbacks;

import hla.rti1516_2025.time.HLAinteger64Time;

final class TimeConstrainedEnabledCallback extends AbstractCallback<HLAinteger64Time> {

    TimeConstrainedEnabledCallback(HLAinteger64Time outcome) {
        super(outcome, 1);
    }

    /*
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
     */
}
