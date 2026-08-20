package org.see.skf.core;

import hla.rti1516_2025.exceptions.*;

import java.io.File;

public abstract class SEEFederate extends SKFederateBase {

    private final String[] requiredObjectNames;

    protected SEEFederate(File configurationFile, String... requiredObjectNames) {
        super(configurationFile);
        this.requiredObjectNames = requiredObjectNames;
    }

    @Override
    public final void configureAndStart() throws RTIexception {
        connectToRti();
        joinFederationExecution();
        declareSRFOMExecutiveClasses();

        // Publish/subscribe user specified object and interaction classes as well as await discovery of required objects.
        declareClasses();
        declareObjectInstances();
        waitForRequiredObjects();

        setupTimeManagement();
        exec();
    }

    // TODO - Enable MTR support.
    private void declareSRFOMExecutiveClasses() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        subscribeObjectClass("HLAobjectRoot.ExecutionConfiguration", "root_frame_name", "scenario_time_epoch", "current_execution_mode", "next_execution_mode", "next_mode_scenario_time", "next_mode_cte_time", "least_common_time_step");
        publishInteractionClass("HLAinteractionRoot.ModeTransitionRequest", ModeTransitionRequest.class);
    }

    private void waitForRequiredObjects() {

    }

    protected abstract void declareClasses() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, FederateServiceInvocationsAreBeingReportedViaMOM;

    protected abstract void declareObjectInstances();
}
