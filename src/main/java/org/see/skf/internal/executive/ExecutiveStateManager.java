package org.see.skf.internal.executive;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.*;
import org.see.skf.internal.SRFOMSynchronizationPoint;
import org.see.skf.internal.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

        this.federate.addSyncPointListener(SRFOMSynchronizationPoint.MTR_RUN.getLabel(), createRunModeAnnouncedListener());
    }

    private void init() {
        // Perfectly safe cast because we properly vet the existence and type for the ExCO object instance early on.
        ExecutionConfiguration exCO = (ExecutionConfiguration) this.federate.queryRemoteObjectInstance("ExCO");

        if (exCO == null) {
            throw new ExCONotInitializedException("Cannot proceed with federate execution because ExCO attribute values were not properly initialized.");
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

                logger.info("Federate now operating in the execution mode: {}.", this.localExecutionMode);
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

    private SyncPointListener createRunModeAnnouncedListener() {
        return new SyncPointListener() {
            @Override
            public void announced() {
                String runModeTransitionLabel = SRFOMSynchronizationPoint.MTR_RUN.getLabel();

                try {
                    federate.achieveSynchronizationPoint(runModeTransitionLabel);
                    logger.debug("Achieved SRFOM <{}> sync point.", runModeTransitionLabel);
                } catch (RTIexception e) {
                    logger.error("Failed to achieve the SRFOM synchronization point <{}>.", runModeTransitionLabel, e);
                }

                changeExecutionMode(ExecutionMode.EXEC_MODE_RUNNING);
            }

            @Override
            public void federationSynchronized() {
                // Ignore.
            }
        };
    }
}
