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

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.FederateInternalError;
import hla.rti1516_2025.time.HLAinteger64Time;
import hla.rti1516_2025.time.LogicalTime;
import org.see.skf.internal.FederateMapping;
import org.see.skf.internal.InternalObjectBuilderException;
import org.see.skf.internal.SyncPointManager;
import org.see.skf.internal.runtime.HLAInteractionManager;
import org.see.skf.internal.callbacks.HLACallbackManager;
import org.see.skf.internal.runtime.HLAObjectManager2;

import java.util.concurrent.ExecutorService;

final class SKFederateAmbassador extends NullFederateAmbassador {

    private final FederateMapping federateMapping;
    private final HLACallbackManager callbackManager;
    private final HLAObjectManager2 objectManager;
    private final HLAInteractionManager interactionManager;
    private final SyncPointManager syncPointManager;
    private final ExecutorService executor;

    private SKFederateAmbassador(Builder builder) {
        this.executor = builder.executor;
        this.callbackManager = builder.callbackManager;
        this.objectManager = builder.objectManager;
        this.interactionManager = builder.interactionManager;
        this.syncPointManager = builder.syncPointManager;
        this.federateMapping = builder.federateMapping;
    }

    // N.B. This operation is NOT safe to launch in ExecutorService because of a contentious race condition where the RTI service thread will
    // relay updates via the reflectAttributeValues callback before this thread is done creating the internal object instance representations for the federate.
    @Override
    public void discoverObjectInstance(ObjectInstanceHandle objectInstance, ObjectClassHandle objectClass, String objectInstanceName, FederateHandle producingFederate) throws FederateInternalError {
        this.federateMapping.get(producingFederate);
        this.objectManager.remoteObjectInstanceDiscovered(objectInstance, objectInstanceName, objectClass);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstance, byte[] userSuppliedTag, FederateHandle producingFederate) throws FederateInternalError {
        this.executor.submit(() -> remoteObjectInstanceDestroyed(objectInstance, producingFederate));
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstance, byte[] userSuppliedTag, FederateHandle producingFederate, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) throws FederateInternalError {
        this.executor.submit(() -> remoteObjectInstanceDestroyed(objectInstance, producingFederate));
    }

    private void remoteObjectInstanceDestroyed(ObjectInstanceHandle objectInstance, FederateHandle producingFederate) {
        String producingFederateName = this.federateMapping.get(producingFederate);
        this.objectManager.remoteObjectInstanceDestroyed(objectInstance, producingFederateName);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstance, AttributeHandleValueMap attributeValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions) throws FederateInternalError {
        this.executor.submit(() -> reflectAttributeValueCallback(objectInstance, attributeValues, producingFederate));
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstance, AttributeHandleValueMap attributeValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) throws FederateInternalError {
        this.executor.submit(() -> reflectAttributeValueCallback(objectInstance, attributeValues, producingFederate));
    }

    private void reflectAttributeValueCallback(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributeValues, FederateHandle producingFederate) {
        this.executor.submit(() -> {
            String producingFederateName = this.federateMapping.get(producingFederate);
            boolean broadcastInstanceDiscoveryComplete = this.callbackManager.completeInstanceDiscoveryValueAcquisitionCallback(instanceHandle);
            this.objectManager.remoteObjectInstanceUpdated(instanceHandle, attributeValues, producingFederateName, broadcastInstanceDiscoveryComplete);
        });
    }

    // TODO
    @Override
    public void provideAttributeValueUpdate(ObjectInstanceHandle objectInstance, AttributeHandleSet attributes, byte[] userSuppliedTag) throws FederateInternalError {

    }

    @Override
    public void objectInstanceNameReservationSucceeded(String objectInstanceName) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeNameReservationCallback(objectInstanceName, true));
    }

    @Override
    public void objectInstanceNameReservationFailed(String objectInstanceName) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeNameReservationCallback(objectInstanceName, false));
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions) throws FederateInternalError {
        interactionReceived(interactionClass, parameterValues, producingFederate);
    }

    private void interactionReceived(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, FederateHandle producingFederate) throws FederateInternalError {
        this.executor.submit(() -> {
            String producingFederateName = this.federateMapping.get(producingFederate);
            this.interactionManager.interactionReceived(interactionClass, parameterValues, producingFederateName);
        });
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) throws FederateInternalError {
        interactionReceived(interactionClass, parameterValues, producingFederate);
    }

    @Override
    public void timeConstrainedEnabled(LogicalTime<?, ?> time) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeTimeConstrainedCallback((HLAinteger64Time) time));
    }

    @Override
    public void timeRegulationEnabled(LogicalTime<?, ?> time) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeTimeRegulationCallback((HLAinteger64Time) time));
    }

    @Override
    public void timeAdvanceGrant(LogicalTime<?, ?> time) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeTimeAdvanceGrantCallback((HLAinteger64Time) time));
    }

    @Override
    public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] userSuppliedTag) throws FederateInternalError {
        this.executor.submit(() -> this.syncPointManager.syncPointAnnounced(synchronizationPointLabel));
    }

    @Override
    public void federationSynchronized(String synchronizationPointLabel, FederateHandleSet failedToSyncSet) throws FederateInternalError {
        this.executor.submit(() -> this.syncPointManager.federationSynchronized(synchronizationPointLabel));
    }

    static final class Builder {

        private ExecutorService executor;

        private HLACallbackManager callbackManager;

        private HLAObjectManager2 objectManager;

        private HLAInteractionManager interactionManager;

        private SyncPointManager syncPointManager;

        private FederateMapping federateMapping;

        Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        Builder callbackManager(HLACallbackManager callbackManager) {
            this.callbackManager = callbackManager;
            return this;
        }

        Builder objectManager(HLAObjectManager2 objectManager) {
            this.objectManager = objectManager;
            return this;
        }

        Builder interactionManager(HLAInteractionManager interactionManager) {
            this.interactionManager = interactionManager;
            return this;
        }

        Builder syncPointManager(SyncPointManager syncPointManager) {
            this.syncPointManager = syncPointManager;
            return this;
        }

        Builder federateMapping(FederateMapping federateMapping) {
            this.federateMapping = federateMapping;
            return this;
        }

        SKFederateAmbassador build() {
            if (this.callbackManager == null || this.objectManager == null || this.interactionManager == null || this.federateMapping == null || this.executor == null || this.syncPointManager == null) {
                throw new InternalObjectBuilderException("Missing one or more arguments required to initialize internal federate ambassador object.");
            }

            return new SKFederateAmbassador(this);
        }
    }
}
