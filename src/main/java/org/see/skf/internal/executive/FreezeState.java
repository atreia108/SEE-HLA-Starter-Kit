package org.see.skf.internal.executive;

import org.see.skf.core.ExecutionMode;
import org.see.skf.core.SKFederateBase;
import org.see.skf.internal.TimeManager;

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
