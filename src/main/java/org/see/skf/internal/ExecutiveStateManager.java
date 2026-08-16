package org.see.skf.internal;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.ExecutionConfiguration;
import org.see.skf.core.ExecutionMode;
import org.see.skf.core.SKFederateBase;

public final class ExecutiveStateManager {

    private final SKFederateBase federate;
    private final ExecutiveState runState;
    private final TimeManager timeManager;
    private final ExecutiveState freezeState;

    private ExecutiveState currentExecutiveState;
    private ExecutiveState nextExecutiveState;

    public ExecutiveStateManager(SKFederateBase federate, TimeManager timeManager) {
        this.federate = federate;
        this.timeManager = timeManager;
        this.runState = new RunState(federate, timeManager);
        this.freezeState = new FreezeState(federate, timeManager);
    }

    private void init() {
        // Perfectly safe cast because we properly vet the type of ExCO object instance early on.
        ExecutionConfiguration exCO = (ExecutionConfiguration) this.federate.queryRemoteObjectInstance("ExCO");
        if (exCO == null) {
            throw new IllegalStateException("Cannot proceed with federate execution because ExCO attribute values were not properly initialized.");
        }

        // Checks to handle premature state transitions when the federate just joins, especially in the case of late joiners.
        // TODO

        this.currentExecutiveState = this.runState;
    }

    public void run() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        init();

        while (this.currentExecutiveState != null) {
            this.currentExecutiveState.update();

            ExecutionMode currentExecutionmode = this.currentExecutiveState.getExecutionMode();
            ExecutionMode nextExecutionMode = this.nextExecutiveState != null ? this.nextExecutiveState.getExecutionMode() : null;

            if (nextExecutionMode != null && (currentExecutionmode != nextExecutionMode)) {
                this.currentExecutiveState.transition(nextExecutionMode);
                this.currentExecutiveState = nextExecutiveState;
                this.nextExecutiveState = null;
            }
        }

        federate.processShutdownJobs();
        federate.shutdownExecution();
    }

    public synchronized void switchState(ExecutionMode executionMode) {
        switch (executionMode) {
            case EXEC_MODE_RUNNING:
                this.nextExecutiveState = this.runState;
                break;
            case EXEC_MODE_FREEZE:
                this.nextExecutiveState = this.freezeState;
                break;
            case EXEC_MODE_SHUTDOWN:
                this.currentExecutiveState = null;
                break;
            default:
                // Ignore other cases, which are going to be irrelevant to the state switches we're specifically interested in.
                break;
        }
    }
}
