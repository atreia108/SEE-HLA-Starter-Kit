package org.see.skf.internal.callbacks;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import hla.rti1516_2025.time.HLAinteger64Interval;
import hla.rti1516_2025.time.HLAinteger64Time;
import org.see.skf.internal.HLAUtilityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.*;

public final class FederateCallbackManager {

    private static final Logger logger = LoggerFactory.getLogger(FederateCallbackManager.class);

    private final RTIambassador rtiAmbassador;

    private final ExecutorService executor;

    private final Set<FederateBiCallback<String, Boolean>> nameReservationCallbacks;
    private FederateCallback<HLAinteger64Time> timeConstrainedEnabledCallback;
    private FederateCallback<HLAinteger64Time> timeRegulationEnabledCallback;
    private FederateCallback<HLAinteger64Time> timeAdvanceGrantCallback;

    public FederateCallbackManager(ExecutorService executor) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.executor = executor;

        this.nameReservationCallbacks = new CopyOnWriteArraySet<>();
    }

    public Future<Boolean> invokeNameReservationCallback(String objectInstanceName) throws FederateNotExecutionMember, RestoreInProgress, IllegalName, NotConnected, RTIinternalError, SaveInProgress {
        FederateBiCallback<String, Boolean> callback = new BiCallbackImpl<>(objectInstanceName,1);
        FutureTask<Boolean> task = callback.getTask();
        this.nameReservationCallbacks.add(callback);
        this.executor.submit(task);

        rtiAmbassador.reserveObjectInstanceName(objectInstanceName);

        return task;
    }

    public void completeNameReservationCallback(String objectInstanceName, boolean outcomeValue) {
        for (FederateBiCallback<String, Boolean> callback : this.nameReservationCallbacks) {
            if (callback.getTarget().equals(objectInstanceName)) {
                callback.complete(outcomeValue);
                this.nameReservationCallbacks.remove(callback);
            }
        }
    }

    public Future<HLAinteger64Time> invokeTimeConstrainedCallback() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.timeConstrainedEnabledCallback = new FederateCallbackImpl<>(1);
        FutureTask<HLAinteger64Time> task = this.timeConstrainedEnabledCallback.getTask();
        this.executor.submit(task);

        try {
            rtiAmbassador.enableTimeConstrained();
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
        this.timeRegulationEnabledCallback = new FederateCallbackImpl<>(1);
        FutureTask<HLAinteger64Time> task = this.timeRegulationEnabledCallback.getTask();
        this.executor.submit(task);

        try {
            rtiAmbassador.enableTimeRegulation(lookaheadInLogicalTime);
        } catch (InTimeAdvancingState | RequestForTimeRegulationPending | TimeRegulationAlreadyEnabled e) {
            logger.warn("Redundant attempt to time regulate this federate.", e);
        } catch (InvalidLookahead e) {
            throw new RuntimeException(e);
        }

        return task;
    }

    public void completeTimeRegulationCallback(HLAinteger64Time newLogicalTime) {
        if (this.timeRegulationEnabledCallback != null) {
            this.timeRegulationEnabledCallback.complete(newLogicalTime);
            this.timeRegulationEnabledCallback = null;
        }
    }

    public Future<HLAinteger64Time> invokeTimeAdvanceGrantCallback(HLAinteger64Time nextLogicalTime) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.timeAdvanceGrantCallback = new FederateCallbackImpl<>(1);
        FutureTask<HLAinteger64Time> task = this.timeAdvanceGrantCallback.getTask();
        this.executor.submit(task);

        try {
            rtiAmbassador.timeAdvanceRequest(nextLogicalTime);
        } catch (LogicalTimeAlreadyPassed | InvalidLogicalTime e) {
            throw new RuntimeException("Federate time is completely out of sync with the federation execution time.", e);
        } catch (InTimeAdvancingState | RequestForTimeRegulationPending | RequestForTimeConstrainedPending e) {
            logger.warn("Unsuccessful attempt to advance federate time.", e);
        }

        return task;
    }

    public void completeTimeAdvanceGrantCallback(HLAinteger64Time grantedTime) {
        if (this.timeAdvanceGrantCallback != null) {
            this.timeAdvanceGrantCallback.complete(grantedTime);
            this.timeAdvanceGrantCallback = null;
        }
    }
}
