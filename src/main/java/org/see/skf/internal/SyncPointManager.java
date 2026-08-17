package org.see.skf.internal;

import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.FederationSynchronizedListener;
import org.see.skf.core.SyncPointAnnouncementListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class SyncPointManager {

    private final RTIambassador rtiAmbassador;

    private static final Logger logger = LoggerFactory.getLogger(SyncPointManager.class);

    private final Set<SyncPointAnnouncementListener> syncPointAnnouncementListeners;
    private final Set<FederationSynchronizedListener> federationSynchronizedListeners;

    public SyncPointManager() {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.syncPointAnnouncementListeners = new CopyOnWriteArraySet<>();
        this.federationSynchronizedListeners = new CopyOnWriteArraySet<>();
    }

    public void achieveSyncPoint(String label) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        try {
            rtiAmbassador.synchronizationPointAchieved(label);
        } catch (SynchronizationPointLabelNotAnnounced e) {
            logger.warn("Cannot achieve the synchronization point <{}> because it has not yet been announced.", label);
        }
    }

    public void achieveSyncPoint(String label, boolean outcome) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        try {
            rtiAmbassador.synchronizationPointAchieved(label, outcome);
        } catch (SynchronizationPointLabelNotAnnounced e) {
            if (outcome) {
                logger.warn("Could not notify RTI of successful achievement of the synchronization point {} because it has not yet been announced.", label);
            } else {
                logger.warn("Could not notify RTI of unsuccessful achievement of the synchronization point {} because it has not yet been announced.", label);
            }
        }
    }

    public void addSyncPointAnnouncementListener(SyncPointAnnouncementListener listener) {
        this.syncPointAnnouncementListeners.add(listener);
    }

    public void removeSyncPointAnnouncementListener(SyncPointAnnouncementListener listener) {
        this.syncPointAnnouncementListeners.remove(listener);
    }

    public void addFederationSynchronizedSyncPointListener(FederationSynchronizedListener listener) {
        this.federationSynchronizedListeners.add(listener);
    }

    public void removeFederationSynchronizedSyncPointListener(FederationSynchronizedListener listener) {
        this.federationSynchronizedListeners.remove(listener);
    }

    public void syncPointAnnounced(String label) {
        this.syncPointAnnouncementListeners.forEach(listener -> listener.announced(label));
    }

    public void federationSynchronized(String label) {
        this.federationSynchronizedListeners.forEach(listener -> listener.synced(label));
    }
}
