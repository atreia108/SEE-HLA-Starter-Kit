/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java framework for developing
 SRFOM-compliant HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London (UK). All rights reserved.

 GNU Lesser General Public License (GNU LGPL).

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3.0 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library.
 If not, see http://http://www.gnu.org/licenses/
 *****************************************************************/

package org.see.skf.core;

import hla.rti1516_2025.exceptions.*;

import java.beans.PropertyChangeListener;
import java.util.Map;
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

    String createObjectInstance(Object objectInstance) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    Future<Void> createObjectInstance(Object objectInstance, String name);

    void updateObjectInstance(Object objectInstance, String... attributes) throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectInstanceNotKnown;

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

    void sendInteraction(Object interaction) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined;

    void addInteractionListener(InteractionListener listener);

    void removeInteractionListener(InteractionListener listener);

    void achieveSyncPoint(String label) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void achieveSyncPoint(String label, boolean success) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    void addSyncPointListener(String label, SyncPointListener listener);

    void removeSyncPointListener(SyncPointListener listener);

    Future<Map<String, String>> queryAttributeOwnership(String objectInstanceName, String... attributeNames);

    void acquireAttributeOwnership(String objectInstanceName, String... attributeNames) throws FederateNotExecutionMember, ObjectClassNotPublished, AttributeNotDefined, RestoreInProgress, FederateOwnsAttributes, ObjectInstanceNotKnown, NotConnected, RTIinternalError, AttributeNotPublished, SaveInProgress;

    void divestAttributeOwnershipIfWanted(Object objectInstance, String... attributeNames) throws FederateNotExecutionMember, AttributeNotDefined, RestoreInProgress, AttributeNotOwned, ObjectInstanceNotKnown, NotConnected, RTIinternalError, SaveInProgress;

    void addAttributeOwnershipListener(Object objectInstance, AttributeOwnershipListener listener);

    void removeAttributeOwnershipListener(AttributeOwnershipListener listener);

    double getSimulationTime();

    boolean isAdvancing();

    String getName();

    String getType();

    enum Role {
        EARLY,
        LATE
    }
}
