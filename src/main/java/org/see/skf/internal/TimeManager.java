package org.see.skf.internal;

import hla.rti1516_2025.exceptions.*;
import hla.rti1516_2025.time.HLAinteger64Interval;
import hla.rti1516_2025.time.HLAinteger64Time;
import hla.rti1516_2025.time.HLAinteger64TimeFactory;

import org.see.skf.core.TimeInitializationFailure;
import org.see.skf.internal.callbacks.HLACallbackManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class TimeManager {

    private static final Logger logger = LoggerFactory.getLogger(TimeManager.class);

    private static final double UNINITIALIZED_JDT_VALUE = -1.0;
    private static final double HLT_PER_SECOND = 1000000.0;

    private HLAinteger64TimeFactory timeFactory;

    private final HLACallbackManager callbackManager;

    private final long lookahead;

    private double simulationElapsedTime;

    private double federationScenarioTimeEpoch;

    private double simulationScenarioTimeEpoch;

    private double simulationScenarioTime;

    private HLAinteger64Time logicalTime;

    public TimeManager(long lookahead, HLACallbackManager callbackManager) {
        this.lookahead = lookahead;
        this.callbackManager = callbackManager;

        this.logicalTime = null;
        this.simulationElapsedTime = 0.0;
        this.federationScenarioTimeEpoch = UNINITIALIZED_JDT_VALUE;
        this.simulationScenarioTimeEpoch = UNINITIALIZED_JDT_VALUE;
        this.simulationScenarioTime = UNINITIALIZED_JDT_VALUE;
    }

    private double calculateSimulationScenarioTime(double scenarioTimeEpoch, HLAinteger64Time logicalTime) {
        return scenarioTimeEpoch + (logicalTime.getValue() / HLT_PER_SECOND);
    }

    public void constrainTime() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        Future<HLAinteger64Time> task = this.callbackManager.invokeTimeConstrainedCallback();

        if (task != null) {
            HLAinteger64Time newLogicalTime;
            try {
                newLogicalTime = task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TimeInitializationFailure("Federate could not be time constrained.", e);
            } catch (ExecutionException e) {
                throw new TimeInitializationFailure("Unexpected exception thrown while trying to time constrain federate", e);
            }

            this.logicalTime = newLogicalTime;
            logger.info("Federate is now time constrained.");
        }
    }

    public void regulateTime() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (this.timeFactory == null) {
            throw new IllegalStateException("Cannot enable time regulation as no valid time factory has been set.");
        }

        HLAinteger64Interval lookaheadInLogicalTime = this.timeFactory.makeInterval(lookahead);
        Future<HLAinteger64Time> task = this.callbackManager.invokeTimeRegulationCallback(lookaheadInLogicalTime);

        if (task != null) {
            HLAinteger64Time newLogicalTime;
            try {
                newLogicalTime = task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TimeInitializationFailure("Federate could not be time constrained.", e);
            } catch (ExecutionException e) {
                throw new TimeInitializationFailure("Unexpected exception thrown while trying to time constrain federate", e);
            }

            this.logicalTime = newLogicalTime;
        }

        logger.info("Federate is now time regulated.");
    }

    public void advanceTime() {
        if (this.timeFactory == null) {
            throw new IllegalStateException("Cannot enable time regulation as no valid time factory has been set.");
        }

        if (this.simulationScenarioTimeEpoch == UNINITIALIZED_JDT_VALUE) {
            // TODO - set the SST0 to the new logical time.
        }
    }

    public void advanceToLogicalTimeBoundary() {
        if (this.timeFactory == null) {
            throw new IllegalStateException("Cannot enable time regulation as no valid time factory has been set.");
        }

        if (this.simulationScenarioTimeEpoch == UNINITIALIZED_JDT_VALUE) {
            // TODO - set the SST0 to the new logical time.
        }
    }

    public double getSimulationScenarioTime() {
        return this.simulationScenarioTime;
    }

    public void setFederationScenarioTimeEpoch(double federationScenarioTimeEpoch) {
        this.federationScenarioTimeEpoch = federationScenarioTimeEpoch;
    }

    public void setTimeFactory(HLAinteger64TimeFactory timeFactory) {
        this.timeFactory = timeFactory;
    }
}
