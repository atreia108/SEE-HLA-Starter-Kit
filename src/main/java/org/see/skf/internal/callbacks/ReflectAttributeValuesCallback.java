package org.see.skf.internal.callbacks;

import hla.rti1516_2025.AttributeHandleValueMap;
import hla.rti1516_2025.ObjectInstanceHandle;

import java.util.concurrent.FutureTask;

public final class ReflectAttributeValuesCallback extends AbstractBiCallback<ObjectInstanceHandle, AttributeHandleValueMap> {

    ReflectAttributeValuesCallback(ObjectInstanceHandle target, AttributeHandleValueMap outcome) {
        super(target, outcome);
    }

    @Override
    protected FutureTask<AttributeHandleValueMap> createTask() {
        return new FutureTask<>(() -> {
            while (true) {
                AttributeHandleValueMap outcome = getOutcome();
                if (outcome != null) {
                    return outcome;
                }
            }
        });
    }
}
