package org.see.skf.internal.executive;

import org.see.skf.core.ExecutionMode;
import org.see.skf.core.FederationSynchronizedListener;
import org.see.skf.core.SKFederateBase;
import org.see.skf.internal.SRFOMSynchronizationPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public final class FreezeState implements TransitiveState {

    private static final Logger logger = LoggerFactory.getLogger(FreezeState.class);

    private final SKFederateBase federate;

    public FreezeState(SKFederateBase federate) {
        this.federate = federate;
    }

    @Override
    public void transition(ExecutionMode nextExecutionMode) {
        if (nextExecutionMode == ExecutionMode.EXEC_MODE_RUNNING) {
            String runModeTransitionLabel = SRFOMSynchronizationPoint.MTR_RUN.getLabel();
            CountDownLatch latch = new CountDownLatch(1);

            FederationSynchronizedListener federationSynchronizedListener = createFederationSynchronizedListener(runModeTransitionLabel, latch);
            this.federate.addFederationSynchronizedSyncPointListener(federationSynchronizedListener);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Federate thread interrupted while waiting for federation synchronization of the SRFOM <" + runModeTransitionLabel + "> synchronization point.", e);
            }

            this.federate.removeFederationSynchronizedSyncPointListener(federationSynchronizedListener);
        }
    }

    private FederationSynchronizedListener createFederationSynchronizedListener(String label, CountDownLatch latch) {
        return syncPointLabel -> {
            if (syncPointLabel.equals(label)) {
                latch.countDown();
            }
        };
    }
}
