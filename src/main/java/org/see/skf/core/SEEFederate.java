package org.see.skf.core;

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
        // initializeExCO();
        declareClasses();
        // declareObjectInstances();
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

    protected abstract void declareClasses();

    protected abstract void declareObjectInstances();
}
