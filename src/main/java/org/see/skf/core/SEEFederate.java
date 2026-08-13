package org.see.skf.core;

import hla.rti1516_2025.exceptions.*;

import java.io.File;
import java.util.concurrent.ExecutionException;

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
        ExecutionConfiguration exCO = initializeExCO();

        // Publish/subscribe user specified object and interaction classes as well as await discovery of required objects.
        declareClasses();
        declareObjectInstances();
        waitForRequiredObjects();

        setupTimeManagement(exCO.getScenarioTimeEpoch());
        advanceToLogicalTimeBoundary(exCO.getLeastCommonTimeStep());
    }

    // TODO - Enable MTR support.
    private void declareSRFOMExecutiveClasses() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        subscribeObjectClass("HLAobjectRoot.ExecutionConfiguration", "root_frame_name", "scenario_time_epoch", "current_execution_mode", "next_execution_mode", "next_mode_scenario_time", "next_mode_cte_time", "least_common_time_step");
        // publishInteractionClass("HLAinteractionRoot.ExecutionConfiguration", "execution_mode");
    }

    private ExecutionConfiguration initializeExCO() {
        ExecutionConfiguration exCO = new ExecutionConfiguration();

        try {
            exCO = trackRemoteObjectInstance(exCO, "ExCO").get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to properly initialize ExCO object instance.", e);
        }

        return exCO;
    }

    private void waitForRequiredObjects() {

    }

    protected abstract void declareClasses();

    protected abstract void declareObjectInstances();
}
