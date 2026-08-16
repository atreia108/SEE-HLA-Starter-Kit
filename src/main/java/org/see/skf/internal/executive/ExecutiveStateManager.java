package org.see.skf.internal.executive;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.ExecutionConfiguration;
import org.see.skf.core.ExecutionMode;
import org.see.skf.core.SKFederateBase;
import org.see.skf.internal.TimeManager;

public final class ExecutiveStateManager {

    private final SKFederateBase federate;
    private final ExecutiveState runState;
    private final ExecutiveState freezeState;

    private ExecutiveState executiveState;
    private ExecutionMode localExecutionMode;
    private ExecutionMode nextExecutionMode;

    public ExecutiveStateManager(SKFederateBase federate, TimeManager timeManager) {
        this.federate = federate;
        this.runState = new RunState(federate, timeManager);
        this.freezeState = new FreezeState(federate, timeManager);
    }

    private void init() {
        // Perfectly safe cast because we properly vet the existence and type for the ExCO object instance early on.
        ExecutionConfiguration exCO = (ExecutionConfiguration) this.federate.queryRemoteObjectInstance("ExCO");
        if (exCO == null) {
            throw new IllegalStateException("Cannot proceed with federate execution because ExCO attribute values were not properly initialized.");
        }

        // Checks to handle premature state transitions when the federate just joins, especially in the case of late joiners.
        // TODO

        this.localExecutionMode = exCO.getCurrentExecutionMode();
        this.nextExecutionMode = exCO.getNextExecutionMode();

        this.executiveState = getExecutiveStateForMode(this.localExecutionMode);
    }

    public void run() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        init();

        while (this.localExecutionMode != ExecutionMode.EXEC_MODE_SHUTDOWN && this.executiveState != null) {
            this.executiveState.update();

            if (this.localExecutionMode != this.nextExecutionMode) {
                this.executiveState.transition(this.nextExecutionMode);
                this.executiveState = getExecutiveStateForMode(this.nextExecutionMode);

                this.localExecutionMode = this.nextExecutionMode;
            }
        }

        federate.processShutdownJobs();
        federate.shutdownExecution();
    }

    private ExecutiveState getExecutiveStateForMode(ExecutionMode executionMode) {
        switch (executionMode) {
            case EXEC_MODE_RUNNING:
                return this.runState;
            case EXEC_MODE_FREEZE:
                return this.freezeState;
            default:
                return null;
        }
    }

    public synchronized void changeExecutionMode(ExecutionMode executionMode) {
        this.nextExecutionMode = executionMode;
    }
}
