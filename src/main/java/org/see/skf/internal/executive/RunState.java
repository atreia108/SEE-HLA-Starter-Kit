package org.see.skf.internal.executive;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.*;
import org.see.skf.core.ExCONotInitializedException;
import org.see.skf.internal.SRFOMSynchronizationPoint;
import org.see.skf.internal.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public final class RunState implements TransitiveState {

    private static final Logger logger = LoggerFactory.getLogger(RunState.class);

    private final SKFederateBase federate;
    private final TimeManager timeManager;

    public RunState(SKFederateBase federate, TimeManager timeManager) {
        this.federate = federate;
        this.timeManager = timeManager;
    }

    @Override
    public void transition(ExecutionMode nextExecutionMode) throws RTIexception {
        if (nextExecutionMode == ExecutionMode.EXEC_MODE_FREEZE) {
            CountDownLatch latch = new CountDownLatch(1);
            SyncPointListener listener = createFreezeModeSyncPointListener(latch);

            this.federate.addSyncPointListener(SRFOMSynchronizationPoint.MTR_FREEZE.getLabel(), listener);

            ExecutionConfiguration exCO = (ExecutionConfiguration) this.federate.queryRemoteObjectInstance("ExCO");
            if (exCO == null) {
                throw new ExCONotInitializedException("Cannot perform federate executive state transition as ExCO object instance values could not be retrieved in time.");
            }

            double nextModeScenarioTime = exCO.getNextModeScenarioTime();

            double timeToFreeze = nextModeScenarioTime - this.timeManager.getSimulationScenarioTime();
            while (timeToFreeze > 0.0) {
                this.federate.processRunJobs();
                this.timeManager.advanceTime();

                timeToFreeze -= 1.0;
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Federate thread interrupted while waiting for federation synchronization of MTR_FREEZE synchronization point.", e);
            }

            // Reset the following values for future freeze transitions.
            exCO.setNextExecutionMode(null);
            exCO.setNextModeScenarioTime(null);

            this.federate.removeSyncPointListener(listener);
        }
    }

    private SyncPointListener createFreezeModeSyncPointListener(CountDownLatch latch) {
        return new SyncPointListener() {
            @Override
            public void announced() {
                String freezeModeTransitionLabel = SRFOMSynchronizationPoint.MTR_FREEZE.getLabel();

                try {
                    federate.achieveSyncPoint(freezeModeTransitionLabel);
                    logger.debug("Achieved SRFOM <{}> sync point.", freezeModeTransitionLabel);

                    latch.countDown();
                } catch (RTIexception e) {
                    logger.error("Failed to achieve the SRFOM synchronization point <{}>.", freezeModeTransitionLabel, e);
                }
            }

            @Override
            public void federationSynchronized() {
                latch.countDown();
            }
        };
    }
}
