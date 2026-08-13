package org.see.skf.internal.callbacks;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import hla.rti1516_2025.time.HLAinteger64Interval;
import hla.rti1516_2025.time.HLAinteger64Time;
import org.see.skf.core.HLAUtilityFactory;
import org.see.skf.core.TimeInitializationFailure;
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

    private TimeConstrainedEnabledCallback timeConstrainedEnabledCallback;
    private TimeRegulationEnabledCallback timeRegulationEnabledCallback;
    private TimeAdvanceGrantCallback timeAdvanceGrantCallback;

    public HLACallbackManager(ExecutorService executor) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.executor = executor;

        this.nameReservationCallbacks = new CopyOnWriteArraySet<>();
        this.reflectAttributeValuesCallbacks = new CopyOnWriteArraySet<>();
    }

    public Future<Boolean> invokeNameReservationCallback(String objectInstanceName) throws FederateNotExecutionMember, RestoreInProgress, IllegalName, NotConnected, RTIinternalError, SaveInProgress {
        NameReservationCallback callback = new NameReservationCallback(objectInstanceName, null);
        FutureTask<Boolean> task = callback.getTask();
        this.nameReservationCallbacks.add(callback);
        this.executor.submit(task);

        rtiAmbassador.reserveObjectInstanceName(objectInstanceName);

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

    public Future<HLAinteger64Time> invokeTimeConstrainedCallback() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.timeConstrainedEnabledCallback = new TimeConstrainedEnabledCallback(null);
        FutureTask<HLAinteger64Time> task = this.timeConstrainedEnabledCallback.getTask();
        this.executor.submit(task);

        try {
            rtiAmbassador.enableTimeConstrained();
            task = this.timeConstrainedEnabledCallback.getTask();
        } catch (InTimeAdvancingState | RequestForTimeConstrainedPending | TimeConstrainedAlreadyEnabled e) {
            logger.warn("Redundant attempt to time constrain this federate.", e);
        }

        return task;
    }

    public void completeTimeConstrainedCallback(HLAinteger64Time newLogicalTime) {
        if (this.timeConstrainedEnabledCallback != null) {
            this.timeConstrainedEnabledCallback.complete(newLogicalTime);
            this.timeConstrainedEnabledCallback = null;
        }
    }

    public Future<HLAinteger64Time> invokeTimeRegulationCallback(HLAinteger64Interval lookaheadInLogicalTime) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.timeRegulationEnabledCallback = new TimeRegulationEnabledCallback(null);
        FutureTask<HLAinteger64Time> task = this.timeRegulationEnabledCallback.getTask();
        this.executor.submit(task);

        try {
            rtiAmbassador.enableTimeRegulation(lookaheadInLogicalTime);
            task = this.timeRegulationEnabledCallback.getTask();
        } catch (InTimeAdvancingState | RequestForTimeRegulationPending | TimeRegulationAlreadyEnabled e) {
            logger.warn("Redundant attempt to time regulate this federate.", e);
        } catch (InvalidLookahead e) {
            throw new TimeInitializationFailure(e);
        }

        return task;
    }

    public void completeTimeRegulationCallback(HLAinteger64Time newLogicalTime) {
        if (this.timeRegulationEnabledCallback != null) {
            this.timeRegulationEnabledCallback.complete(newLogicalTime);
            this.timeRegulationEnabledCallback = null;
        }
    }

    // TODO
    public void invokeTimeAdvanceGrantCallback(HLAinteger64Time nextLogicalTime) {
        try {
            rtiAmbassador.timeAdvanceRequest(nextLogicalTime);
        } catch (LogicalTimeAlreadyPassed | InvalidLogicalTime | InTimeAdvancingState e) {
            throw new RuntimeException(e);
        }  catch (RequestForTimeRegulationPending | RequestForTimeConstrainedPending e) {
            throw new RuntimeException(e);
        }  catch (SaveInProgress e) {
            throw new RuntimeException(e);
        } catch (RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new RuntimeException(e);
        }
    }

    // TODO
    public void completeTimeAdvanceGrantCallback(HLAinteger64Time grantedTime) {

    }
}
