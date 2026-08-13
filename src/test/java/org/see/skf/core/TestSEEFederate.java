package org.see.skf.core;

import hla.rti1516_2025.exceptions.RTIexception;
import org.see.skf.runtime.models.PhysicalEntity;
import org.see.skf.runtime.models.ReferenceFrame;
import org.see.skf.runtime.models.Vector3;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestSEEFederate extends SEEFederate {
    private static final File confFile = new File("src/test/resources/valid.conf");

    private PhysicalEntity spaceport;
    private ExecutionConfiguration executionConfiguration;
    private ReferenceFrame referenceFrame;

    protected TestSEEFederate(File configurationFile, String... requiredObjects) {
        super(configurationFile, requiredObjects);
    }

    @Override
    protected void declareClasses() {

    }

    @Override
    protected void declareObjectInstances() {
        /*
        this.spaceport = new PhysicalEntity();
        Future<Void> future = createObjectInstance(spaceport, "MoonCentricFixed");
        try {
            future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException ignore) {}

        Vector3 accel = this.spaceport.getAcceleration();
        this.spaceport.setAcceleration(new Vector3(accel.getX() + 1.0, accel.getY() + 1.0, accel.getZ() + 1.0));

        try {
            updateObjectInstance(spaceport, "acceleration");
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
    }

    @Override
    protected void update() {
        /*
        if (executionConfiguration == null) {
            Future<ExecutionConfiguration> exCO = trackRemoteObjectInstance(new ExecutionConfiguration(), "ExCO");

            new Thread(() -> {
                try {
                    this.executionConfiguration = exCO.get();
                    System.out.println("ExCO is now initialized!");
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } else {
            System.out.println("ExCO [ root_frame_name: " + executionConfiguration.getRootFrameName() + ", scenario_time_epoch: " + executionConfiguration.getScenarioTimeEpoch() + ", current_execution_mode: " + executionConfiguration.getCurrentExecutionMode() + " ]");
        }

        if (referenceFrame == null) {
            Future<ReferenceFrame> frame = trackRemoteObjectInstance(new ReferenceFrame(), "SeeLunarSouthPoleBaseLocalFixed");
            new Thread(() -> {
                try {
                    this.referenceFrame = frame.get();
                    System.out.println("Reference Frame [ Name: " + referenceFrame.getName() + ", Parent Name: " + referenceFrame.getParentName() + " ]");
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        try {
            Vector3 accel = this.spaceport.getAcceleration();
            this.spaceport.setAcceleration(new Vector3(accel.getX() + 1.0, accel.getY() + 1.0, accel.getZ() + 1.0));
            updateObjectInstance(spaceport, "acceleration");
        } catch (Exception e) {
            // throw new RuntimeException(e);
        }
         */
    }

    public static void main(String[] args) throws RTIexception {
        new TestSEEFederate(confFile).configureAndStart();
    }
}
