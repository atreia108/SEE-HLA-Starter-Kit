package org.see.skf.internal.callbacks;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.HLAUtilityFactory;
import org.see.skf.internal.runtime.HLAObjectInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.*;

public final class HLACallbackManager {

    private static final Logger logger = LoggerFactory.getLogger(HLACallbackManager.class);

    private final RTIambassador rtiAmbassador;

    private final ExecutorService executor;

    private final Set<NameReservationCallback> nameReservationCallbacks;
    private final Set<ReflectAttributeValuesCallback> reflectAttributeValuesCallbacks;

    public HLACallbackManager(ExecutorService executor) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.executor = executor;

        this.nameReservationCallbacks = new CopyOnWriteArraySet<>();
        this.reflectAttributeValuesCallbacks = new CopyOnWriteArraySet<>();
    }

    public Future<Boolean> invokeNameReservationCallback(String objectInstanceName) {
        NameReservationCallback callback = new NameReservationCallback(objectInstanceName, null);
        FutureTask<Boolean> task = callback.getTask();
        this.nameReservationCallbacks.add(callback);
        this.executor.submit(task);

        try {
            rtiAmbassador.reserveObjectInstanceName(objectInstanceName);
        } catch (IllegalName | SaveInProgress | RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new NameReservationException("The name <" + objectInstanceName + "> could not be reserved.", e);
        }

        return task;
    }

    public void completeNameReservationCallback(String objectInstanceName, boolean outcomeValue) {
        for (NameReservationCallback callback : this.nameReservationCallbacks) {
            if (callback.getTarget().equals(objectInstanceName)) {
                callback.complete(outcomeValue);
                nameReservationCallbacks.remove(callback);
            }
        }
    }

    public Future<AttributeHandleValueMap> invokeReflectAttributeValueCallback(HLAObjectInstance objectInstance, AttributeHandleSet subscribedAttributes) {
        ObjectInstanceHandle instanceHandle = objectInstance.getHandle();

        ReflectAttributeValuesCallback callback = new ReflectAttributeValuesCallback(instanceHandle, null);
        FutureTask<AttributeHandleValueMap> task = callback.getTask();
        this.reflectAttributeValuesCallbacks.add(callback);
        this.executor.submit(task);

        try {
            rtiAmbassador.requestAttributeValueUpdate(instanceHandle, subscribedAttributes, null);
        } catch (AttributeNotDefined | ObjectInstanceNotKnown | SaveInProgress | RestoreInProgress |
                 FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            String instanceName = objectInstance.getName();
            throw new RuntimeException("Failed to send attribute value update request for the discovered object instance <" + instanceName + ">.", e);
        }

        return task;
    }

    public boolean completeReflectAttributeValueCallback(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributeValues) {
        boolean callbackFound = false;
        for (ReflectAttributeValuesCallback callback : this.reflectAttributeValuesCallbacks) {
            ObjectInstanceHandle targetInstanceHandle = callback.getTarget();
            if (targetInstanceHandle.equals(instanceHandle)) {
                callback.complete(attributeValues);
                this.reflectAttributeValuesCallbacks.remove(callback);
                callbackFound = true;
            }
        }
        return callbackFound;
    }
}
