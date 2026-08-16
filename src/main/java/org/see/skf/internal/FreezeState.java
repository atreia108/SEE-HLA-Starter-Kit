package org.see.skf.internal;

import org.see.skf.core.ExecutionConfiguration;
import org.see.skf.core.ExecutionMode;
import org.see.skf.core.SKFederateBase;

public final class FreezeState implements ExecutiveState {

    private final SKFederateBase federate;
    private final TimeManager timeManager;

    public FreezeState(SKFederateBase federate, TimeManager timeManager) {
        this.federate = federate;
        this.timeManager = timeManager;
    }

    @Override
    public void update() {

    }

    @Override
    public void transition(ExecutionMode executionMode) {

    }

    @Override
    public ExecutionMode getExecutionMode() {
        return ExecutionMode.EXEC_MODE_FREEZE;
    }
}
