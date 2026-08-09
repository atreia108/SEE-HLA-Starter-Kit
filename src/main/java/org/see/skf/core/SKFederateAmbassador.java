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
import hla.rti1516_2025.time.LogicalTime;
import org.see.skf.internal.FederateMapping;
import org.see.skf.internal.runtime.HLAObjectInstance;
import org.see.skf.internal.runtime.HLAObjectManager;
import org.see.skf.internal.callbacks.HLACallbackManager;

import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

final class SKFederateAmbassador extends NullFederateAmbassador {

    private final ExecutorService executor;

    private final FederateMapping federateMapping;
    private final HLACallbackManager callbackManager;
    private final HLAObjectManager objectManager;

    private SKFederateAmbassador(Builder builder) {
        this.executor = builder.executor;
        this.callbackManager = builder.callbackManager;
        this.objectManager = builder.objectManager;
        this.federateMapping = builder.federateMapping;
    }

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle objectInstance, ObjectClassHandle objectClass, String objectInstanceName, FederateHandle producingFederate) throws FederateInternalError {
        Runnable r = () -> {
            this.objectManager.remoteObjectInstanceDiscovered(objectInstance, objectInstanceName, objectClass);
            this.federateMapping.add(producingFederate);
        };

        this.executor.submit(r);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstance, AttributeHandleValueMap attributeValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions) throws FederateInternalError {
        reflectAttributeValueCallback(objectInstance, attributeValues);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstance, AttributeHandleValueMap attributeValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) throws FederateInternalError {
        reflectAttributeValueCallback(objectInstance, attributeValues);
    }

    private void reflectAttributeValueCallback(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributeValues) {
        Runnable r = () -> {
            Predicate<HLAObjectInstance> predicate = objectInstance -> objectInstance.getHandle().equals(instanceHandle);
            if (this.objectManager.getObjectInstance(predicate) == null) {
                this.callbackManager.completeReflectAttributeValueCallback(instanceHandle, attributeValues);
            } else {
                this.objectManager.remoteObjectInstanceUpdate(instanceHandle, attributeValues);
            }
        };

        this.executor.submit(r);
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
        this.executor.submit(() ->this.callbackManager.completeNameReservationCallback(objectInstanceName, false));
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions) throws FederateInternalError {

    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) throws FederateInternalError {

    }

    static final class Builder {

        private ExecutorService executor;

        private HLACallbackManager callbackManager;

        private HLAObjectManager objectManager;

        private FederateMapping federateMapping;

        Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        Builder callbackManager(HLACallbackManager callbackManager) {
            this.callbackManager = callbackManager;
            return this;
        }

        Builder objectManager(HLAObjectManager objectManager) {
            this.objectManager = objectManager;
            return this;
        }

        Builder federateMapping(FederateMapping federateMapping) {
            this.federateMapping = federateMapping;
            return this;
        }

        SKFederateAmbassador build() {
            if (this.callbackManager == null || this.objectManager == null || this.federateMapping == null || this.executor == null) {
                throw new IllegalStateException("One or more objects required for initializing SKFederateAmbassador are missing.");
            }

            return new SKFederateAmbassador(this);
        }

    }
}
