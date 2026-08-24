package org.see.skf.internal.executive;

import org.see.skf.core.ExecutionMode;
import org.see.skf.core.SKFederateBase;
import org.see.skf.core.SyncPointListener;
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
            CountDownLatch latch = new CountDownLatch(1);
            SyncPointListener listener = createFederationSynchronizedToFreezeListener(latch);

            // FederationSynchronizedListener federationSynchronizedListener = createFederationSynchronizedListener(runModeTransitionLabel, latch);
            //this.federate.addFederationSynchronizedSyncPointListener(federationSynchronizedListener);

            String runModeTransitionLabel = SRFOMSynchronizationPoint.MTR_RUN.getLabel();
            this.federate.addSyncPointListener(runModeTransitionLabel, listener);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Federate thread interrupted while waiting for federation synchronization of the SRFOM <" + runModeTransitionLabel + "> synchronization point.", e);
            }

            // this.federate.removeFederationSynchronizedSyncPointListener(federationSynchronizedListener);
            this.federate.removeSyncPointListener(listener);
        }
    }

    /*
    private FederationSynchronizedListener createFederationSynchronizedListener(String label, CountDownLatch latch) {
        return syncPointLabel -> {
            if (syncPointLabel.equals(label)) {
                latch.countDown();
            }
        };
    }
     */

    private SyncPointListener createFederationSynchronizedToFreezeListener(CountDownLatch latch) {
        return new SyncPointListener() {
            @Override
            public void announced() {
                // Ignore.
            }

            @Override
            public void federationSynchronized() {
                latch.countDown();
            }
        };
    }
}
