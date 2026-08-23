package org.see.skf.internal.callbacks;

import hla.rti1516_2025.ObjectInstanceHandle;

final class InstanceDiscoveryValueAcquisitionCallback extends AbstractBiCallback<ObjectInstanceHandle, Void> {

    InstanceDiscoveryValueAcquisitionCallback(ObjectInstanceHandle target, Void outcome) {
        super(target, outcome, 1);
    }

    /*
    @Override
    protected FutureTask<Void> createTask() {
        return new FutureTask<>(() -> {
            getLatch().await();
            return null;
        });

        return new FutureTask<>(() -> {
            while (true) {
                AttributeHandleValueMap outcome = getOutcome();
                if (outcome != null) {
                    return outcome;
                }
            }
        });
         */

}
