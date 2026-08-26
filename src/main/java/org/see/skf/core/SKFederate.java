package org.see.skf.core;

import hla.rti1516_2025.exceptions.*;

import java.beans.PropertyChangeListener;
import java.util.concurrent.Future;

public interface SKFederate {

    void configureAndStart() throws RTIexception;

    void connectToRti() throws Unauthorized, ConnectionFailed;

    void joinFederationExecution() throws RestoreInProgress, Unauthorized, NotConnected, RTIinternalError, SaveInProgress, FederateNotExecutionMember;

    void shutdownExecution() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void publishObjectClass(Class<?> representation, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress;

    void unpublishObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress;

    void subscribeObjectClass(Class<?> representation, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress;

    void unsubscribeObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    String createObjectInstance(String objectClassName, Object objectInstance) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    Future<Void> createObjectInstance(String objectClassName, String name, Object objectInstance);

    void updateObjectInstance(Object objectInstance, String... attributes) throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress;

    void destroyObjectInstance(Object objectInstance) throws FederateNotExecutionMember, RestoreInProgress, DeletePrivilegeNotHeld, NotConnected, RTIinternalError, SaveInProgress;

    boolean isRemoteObjectInstanceDiscovered(String name);

    Object queryRemoteObjectInstance(String name);

    void requestRemoteObjectInstanceUpdates(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void addRemoteObjectInstanceListener(String objectInstanceName, RemoteObjectInstanceListener listener);

    void removeRemoteObjectInstanceListener(RemoteObjectInstanceListener listener);

    void addPropertyChangeListener(Object objectInstance, PropertyChangeListener listener);

    void addPropertyChangeListener(Object objectInstance, String propertyName, PropertyChangeListener listener);

    void removePropertyChangeListener(Object objectInstance, String propertyName, PropertyChangeListener listener);

    void removePropertyChangeListener(Object objectInstance, PropertyChangeListener listener);

    void publishInteractionClass(Class<?> proxyClass) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void unpublishInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void subscribeInteractionClass(Class<?> proxyClass) throws FederateNotExecutionMember, RestoreInProgress, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, RTIinternalError, SaveInProgress;

    void unsubscribeInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void sendInteraction(Object interaction) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void addInteractionListener(InteractionListener listener);

    void removeInteractionListener(InteractionListener listener);

    void achieveSyncPoint(String label) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void achieveSyncPoint(String label, boolean success) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void addSyncPointListener(String label, SyncPointListener listener);

    void removeSyncPointListener(SyncPointListener listener);

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
