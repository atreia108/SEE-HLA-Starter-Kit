package org.see.skf.core;

import org.see.skf.runtime.models.PhysicalEntity;
import org.see.skf.runtime.models.ReferenceFrame;
import org.see.skf.runtime.models.Vector3;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestSEEFederate extends SEEFederate {
    private static final File confFile = new File("src/test/resources/test.conf");

    private PhysicalEntity spaceport;
    private ExecutionConfiguration executionConfiguration;
    private ReferenceFrame referenceFrame;

    protected TestSEEFederate(File configurationFile, String... requiredObjects) {
        super(configurationFile, requiredObjects);
    }

    @Override
    protected void declareClasses() {
        try {
            publishObjectClass("HLAobjectRoot.PhysicalEntity",
                    "name", "type", "status", "parent_reference_frame",
                    "state", "acceleration", "rotational_acceleration", "center_of_mass", "body_wrt_structural");

            /*
            subscribeObjectClass("HLAobjectRoot.PhysicalEntity",
                    "name", "type", "status", "parent_reference_frame");

            publishObjectClass("HLAobjectRoot.PhysicalEntity.DynamicalEntity",
                    "name", "type", "status", "parent_reference_frame",
                    "state", "acceleration", "rotational_acceleration", "center_of_mass", "body_wrt_structural",
                    "force", "torque", "mass", "mass_rate", "inertia", "inertia_rate");

            subscribeObjectClass("HLAobjectRoot.PhysicalEntity.DynamicalEntity",
                    "name", "type", "status", "parent_reference_frame",
                    "force", "torque", "mass", "mass_rate", "inertia", "inertia_rate");
             */

            subscribeObjectClass("HLAobjectRoot.ExecutionConfiguration", "root_frame_name", "scenario_time_epoch", "current_execution_mode", "next_execution_mode");
            subscribeObjectClass("HLAobjectRoot.ReferenceFrame", "name", "parent_name");

            this.spaceport = new PhysicalEntity();
            createObjectInstance(spaceport, "brunel_spaceport");

            Vector3 accel = this.spaceport.getAcceleration();
            this.spaceport.setAcceleration(new Vector3(accel.getX() + 1.0, accel.getY() + 1.0, accel.getZ() + 1.0));
            updateObjectInstance(spaceport, "acceleration");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void declareObjectInstances() {

    }

    @Override
    protected void update() {
        if (executionConfiguration == null) {
            Future<ExecutionConfiguration> exCO = queryObjectInstance(new ExecutionConfiguration(), "ExCO");

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
            Future<ReferenceFrame> frame = queryObjectInstance(new ReferenceFrame(), "SeeLunarSouthPoleBaseLocalFixed");
            new Thread(() -> {
                try {
                    this.referenceFrame = frame.get();
                    System.out.println("Reference Frame [ Name: " + referenceFrame.getName() + ", Parent Name: " + referenceFrame.getParentName() + " ]");
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        /*
        try {
            Vector3 accel = this.spaceport.getAcceleration();
            this.spaceport.setAcceleration(new Vector3(accel.getX() + 1.0, accel.getY() + 1.0, accel.getZ() + 1.0));
            updateObjectInstance(spaceport, "acceleration");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
         */
    }

    public static void main(String[] args) {
        TestSEEFederate federate = new TestSEEFederate(confFile);
    }
}
