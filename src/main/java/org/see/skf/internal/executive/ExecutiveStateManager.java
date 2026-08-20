package org.see.skf.internal.executive;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.ExecutionConfiguration;
import org.see.skf.core.ExecutionMode;
import org.see.skf.core.SKFederateBase;
import org.see.skf.internal.SRFOMSynchronizationPoint;
import org.see.skf.internal.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExecutiveStateManager {

    private static final Logger logger = LoggerFactory.getLogger(ExecutiveStateManager.class);

    private final SKFederateBase federate;
    private final TransitiveState runState;
    private final TransitiveState freezeState;

    private final TimeManager timeManager;

    private volatile ExecutionMode localExecutionMode;
    private volatile ExecutionMode nextExecutionMode;

    public ExecutiveStateManager(SKFederateBase federate, TimeManager timeManager) {
        this.federate = federate;
        this.timeManager = timeManager;
        this.runState = new RunState(federate, timeManager);
        this.freezeState = new FreezeState(federate);

        this.federate.addSyncPointAnnouncementListener(syncPointLabel -> {
            String runModeTransitionLabel = SRFOMSynchronizationPoint.MTR_RUN.getLabel();
            if (syncPointLabel.equals(runModeTransitionLabel)) {
                try {
                    this.federate.achieveSynchronizationPoint(runModeTransitionLabel);
                    logger.debug("Achieved SRFOM <{}> sync point.", runModeTransitionLabel);
                } catch (RTIexception e) {
                    logger.error("Failed to achieve the SRFOM synchronization point <{}>.", runModeTransitionLabel, e);
                }

                changeExecutionMode(ExecutionMode.EXEC_MODE_RUNNING);
            }
        });
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
    }

    public void run() throws RTIexception {
        init();


        while (this.localExecutionMode != ExecutionMode.EXEC_MODE_SHUTDOWN) {
            if (this.localExecutionMode != this.nextExecutionMode) {
                TransitiveState state = getTransitiveState(this.localExecutionMode);
                state.transition(this.nextExecutionMode);

                this.localExecutionMode = this.nextExecutionMode;

                logger.info("Federate now operating in the mode: {}.", this.localExecutionMode);
            }

            if (this.localExecutionMode == ExecutionMode.EXEC_MODE_RUNNING) {
                runModeUpdate();
            }
        }

        this.federate.processShutdownJobs();
        this.federate.shutdownExecution();
    }

    private void runModeUpdate() throws RTIexception {
        this.federate.processRunJobs();
        this.timeManager.advanceTime();
    }

    private TransitiveState getTransitiveState(ExecutionMode executionMode) {
        return executionMode == ExecutionMode.EXEC_MODE_RUNNING ? this.runState : this.freezeState;
    }

    public synchronized void changeExecutionMode(ExecutionMode executionMode) {
        this.nextExecutionMode = executionMode;
    }
}
