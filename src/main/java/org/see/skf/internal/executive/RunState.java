package org.see.skf.internal.executive;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.*;
import org.see.skf.internal.SRFOMSynchronizationPoint;
import org.see.skf.internal.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public final class RunState implements SRFOMTransitiveState {

    private static final Logger logger = LoggerFactory.getLogger(RunState.class);

    private final SKFederateBase federate;
    private final TimeManager timeManager;

    public RunState(SKFederateBase federate, TimeManager timeManager) {
        this.federate = federate;
        this.timeManager = timeManager;
    }

    @Override
    public void transition(ExecutionMode nextExecutionMode) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (nextExecutionMode == ExecutionMode.EXEC_MODE_FREEZE) {
            ExecutionConfiguration exCO = (ExecutionConfiguration) this.federate.queryRemoteObjectInstance("ExCO");
            double nextModeScenarioTime = exCO.getNextModeScenarioTime();

            double timeToFreeze = nextModeScenarioTime - this.timeManager.getSimulationScenarioTime();
            while (timeToFreeze > 0.0) {
                this.federate.processRunJobs();
                this.timeManager.advanceTime();

                timeToFreeze -= 1.0;
            }

            String freezeModeTransitionLabel = SRFOMSynchronizationPoint.MTR_FREEZE.getLabel();
            CountDownLatch latch = new CountDownLatch(2);

            SyncPointAnnouncementListener announcementListener = createSyncPointAnnouncementListener(freezeModeTransitionLabel, latch);
            this.federate.addSyncPointAnnouncementListener(announcementListener);

            FederationSynchronizedListener federationSynchronizedListener = createFederationSynchronizedListener(freezeModeTransitionLabel, latch);
            this.federate.addFederationSynchronizedSyncPointListener(federationSynchronizedListener);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Federate thread interrupted while waiting for federation synchronization of MTR_FREEZE synchronization point.", e);
            }

            // Reset the following values for future freeze transitions.
            exCO.setNextExecutionMode(null);
            exCO.setNextModeScenarioTime(null);

            this.federate.removeSyncPointAnnouncementListener(announcementListener);
            this.federate.removeFederationSynchronizedSyncPointListener(federationSynchronizedListener);
        }
    }

    private SyncPointAnnouncementListener createSyncPointAnnouncementListener(String label, CountDownLatch latch) {
        return syncPointLabel -> {
            if (syncPointLabel.equals(label)) {
                try {
                    this.federate.achieveSynchronizationPoint(label);
                    logger.debug("Achieved SRFOM <{}> sync point.", label);

                    latch.countDown();
                } catch (RTIexception e) {
                    logger.error("Failed to achieve the SRFOM synchronization point <{}>.", label, e);
                }
            }
        };
    }

    private FederationSynchronizedListener createFederationSynchronizedListener(String label, CountDownLatch latch) {
        return syncPointLabel -> {
            if (syncPointLabel.equals(label)) {
                latch.countDown();
            }
        };
    }
}
