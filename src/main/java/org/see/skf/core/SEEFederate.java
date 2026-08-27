package org.see.skf.core;

import hla.rti1516_2025.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class SEEFederate extends SKFederateBase {

    private static final Logger logger = LoggerFactory.getLogger(SEEFederate.class);
    private static final long OBJECT_DISCOVERY_WAITING_TIME = 32L;

    private final String[] requiredObjectInstanceNames;
    private final CountDownLatch latch;

    protected SEEFederate(File configurationFile, String... requiredObjectInstanceNames) {
        super(configurationFile);

        if (requiredObjectInstanceNames == null) {
            throw new IllegalArgumentException("Cannot accept the required object instance names as a NULL reference.");
        }

        this.requiredObjectInstanceNames = requiredObjectInstanceNames;
        this.latch = new CountDownLatch(this.requiredObjectInstanceNames.length);
    }

    @Override
    public final void configureAndStart() throws RTIexception {
        connectToRti();
        joinFederationExecution();
        declareSRFOMExecutiveClasses();

        createRequiredObjectInstanceListeners();

        // Publish/subscribe user specified object and interaction classes as well as await discovery of required objects.
        declareClasses();

        // Register object instances this federate will manage.
        declareObjectInstances();

        // Wait to discover all important objects needed by this federate.
        waitForRequiredObjects();
        setupTimeManagement();

        // Enter simulation executive loop.
        exec();
    }

    private void declareSRFOMExecutiveClasses() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        subscribeObjectClass(ExecutionConfiguration.class, "root_frame_name", "scenario_time_epoch", "current_execution_mode", "next_execution_mode", "next_mode_scenario_time", "next_mode_cte_time", "least_common_time_step");
        publishInteractionClass(ModeTransitionRequest.class);
    }

    private void createRequiredObjectInstanceListeners() {
        for (String requiredObjectName : this.requiredObjectInstanceNames) {
            addRemoteObjectInstanceListener(requiredObjectName, new RemoteObjectInstanceListener() {
                @Override
                public void discovered(String producingFederateName) {
                    latch.countDown();
                }

                @Override
                public void initialized(Object instance) {
                    // Ignore.
                }

                @Override
                public void destroyed(String producingFederateName) {
                    // Ignore.
                }
            });
        }
    }

    private void waitForRequiredObjects() {
        if (this.requiredObjectInstanceNames.length > 0) {
            try {
                if (!this.latch.await(OBJECT_DISCOVERY_WAITING_TIME, TimeUnit.SECONDS)) {
                    logger.warn("Not all required object instances were discovered prior to starting federate execution.");
                } else {
                    logger.info("Discovered all required object instances.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while waiting to discover required object instances.");
            }
        }
    }

    protected abstract void declareClasses() throws RTIexception;

    protected abstract void declareObjectInstances() throws RTIexception;
}
