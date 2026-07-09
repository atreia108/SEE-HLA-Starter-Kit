package org.see.skf.core;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class SEEFederate extends SKFederate {
    private final List<String> requiredObjectNames;

    protected SEEFederate(File configurationFile, String... requiredObjects) {
        super(configurationFile);
        requiredObjectNames = Arrays.asList(requiredObjects);

        // TODO - Manage discovery of required objects
    }

    @Override
    public void configureAndStart() {
        initializeExCO();
        declareClasses();
        declareObjectInstances();
        // TODO - Wait for all required objects to be discovered.
        setupTimeManagement();
    }

    private void initializeExCO() {
        // TODO - Subscribe to ExCO object class attributes and enter a blocking loop that waits for the ExCO object instance to be "fully discovered".
    }

    protected abstract void declareClasses();

    protected abstract void declareObjectInstances();
}
