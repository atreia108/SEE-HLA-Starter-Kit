package org.see.skf.core;

import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import hla.rti1516_2025.time.HLAinteger64Time;
import hla.rti1516_2025.time.HLAinteger64TimeFactory;

final class SimulationTime {
    private RTIambassador rtiAmbassador;

    private long lookAhead;
    private long timeCyclesExecuted;
    private HLAinteger64Time federationLogicalTime;
    private double federationScenarioTime;
    private double simulationScenarioTime;
    private double simulationScenarioTimeEpoch;

    private HLAinteger64TimeFactory timeFactory;

    SimulationTime(long lookAhead) {
        try {
            rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();
            rtiAmbassador.getTimeFactory();
        } catch (FederateNotExecutionMember | NotConnected e) {
            throw new FederateStartupException("Failed to procure TimeFactory instance from RTI ambassador for time management.", e);
        }

        this.lookAhead = lookAhead;
    }

    void regulateTime() {
        try {
            rtiAmbassador.enableTimeConstrained();
        } catch (InTimeAdvancingState | FederateNotExecutionMember | NotConnected | RTIinternalError |
                 RequestForTimeConstrainedPending | SaveInProgress | RestoreInProgress e) {
            throw new RuntimeException(e);
        } catch (TimeConstrainedAlreadyEnabled ignore) {}
    }

    void constrainTime() {

    }

    // TODO - Implement equation for HLTB. Throw EXCONotInitializedException if not found yet.
    HLAinteger64Time calculateLogicalTimeBoundary() {
        return null;
    }

    void advanceTime(HLAinteger64Time time) {

    }

    // TODO - Return the latest sim time in TJD.
    synchronized double getTJDTime() {
        return 0.0;
    }

    // TODO - Return the latest sim time as logical time.
    synchronized HLAinteger64Time getLogicalTime() {
        return null;
    }
}
