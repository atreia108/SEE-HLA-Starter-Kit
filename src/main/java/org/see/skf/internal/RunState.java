package org.see.skf.internal;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.ExecutionMode;
import org.see.skf.core.SKFederateBase;

public final class RunState implements ExecutiveState {

    private final SKFederateBase federate;
    private final TimeManager timeManager;

    public RunState(SKFederateBase federate, TimeManager timeManager) {
        this.federate = federate;
        this.timeManager = timeManager;
    }

    @Override
    public void update() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.federate.processRunJobs();
        this.timeManager.advanceTime();
    }

    @Override
    public void transition(ExecutionMode executionMode) {

    }

    @Override
    public ExecutionMode getExecutionMode() {
        return ExecutionMode.EXEC_MODE_RUNNING;
    }
}
