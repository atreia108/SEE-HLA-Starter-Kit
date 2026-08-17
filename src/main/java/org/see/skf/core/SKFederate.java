package org.see.skf.core;

import hla.rti1516_2025.exceptions.*;

import java.beans.PropertyChangeListener;
import java.util.concurrent.Future;

public interface SKFederate {

    void configureAndStart() throws RTIexception;

    void connectToRti() throws Unauthorized, ConnectionFailed;

    void joinFederationExecution() throws RestoreInProgress, Unauthorized, NotConnected, RTIinternalError, SaveInProgress, FederateNotExecutionMember;

    void shutdownExecution() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void publishObjectClass(String className, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress;

    void subscribeObjectClass(String className, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress;

    String createObjectInstance(Object objectInstance) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    Future<Void> createObjectInstance(Object objectInstance, String name);

    void updateObjectInstance(Object objectInstance, String... attributes) throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress;

    <T> Future<T> trackRemoteObjectInstance(T object, String name);

    Object queryRemoteObjectInstance(String name);

    void addObjectInstanceDiscoveredListener(ObjectInstanceDiscoveredListener listener);

    void removeObjectInstanceDiscoveredListener(ObjectInstanceDiscoveredListener listener);

    void addObjectInstanceDestroyedListener(ObjectInstanceDestroyedListener listener);

    void removeObjectInstanceDestroyedListener(ObjectInstanceDestroyedListener listener);

    void addPropertyChangeListener(Object objectInstance, PropertyChangeListener listener);

    void addPropertyChangeListener(Object objectInstance, String propertyName, PropertyChangeListener listener);

    void removePropertyChangeListener(Object objectInstance, String propertyName, PropertyChangeListener listener);

    void removePropertyChangeListener(Object objectInstance, PropertyChangeListener listener);

    void addInteractionListener(InteractionListener listener);

    void removeInteractionListener(InteractionListener listener);

    void achieveSynchronizationPoint(String label) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void achieveSynchronizationPoint(String label, boolean success) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void addSyncPointAnnouncementListener(SyncPointAnnouncementListener listener);

    void removeSyncPointAnnouncementListener(SyncPointAnnouncementListener listener);

    void addFederationSynchronizedSyncPointListener(FederationSynchronizedListener listener);

    void removeFederationSynchronizedSyncPointListener(FederationSynchronizedListener listener);

    double getSimulationTime();

    boolean isAdvancing();

    String getName();

    String getType();

    Role getRole();

    enum Role {
        EARLY,
        LATE
    }
}
