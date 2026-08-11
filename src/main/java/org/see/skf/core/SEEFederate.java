package org.see.skf.core;

import hla.rti1516_2025.exceptions.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class SEEFederate extends SKFederate {

    protected SEEFederate(File configurationFile, String... requiredObjects) {
        super(configurationFile);

        // TODO - Manage discovery of required objects
    }

    @Override
    public void configureAndStart() {
        try {
            connectToRTI();
            joinFederationExecution();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // initializeExCO();
        try {
            declareClasses();
            // declareObjectInstances();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO - Wait for all required objects to be discovered.
        // setupTimeManagement();

        try {
            while (true) {
                update();
                Thread.sleep(1000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeExCO() {
        // TODO - Subscribe to ExCO object class attributes and enter a blocking loop that waits for the ExCO object instance to be "fully discovered".
    }

    protected abstract void declareClasses() throws HLAClassDeclarationException, FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress;

    protected abstract void declareObjectInstances();
}
