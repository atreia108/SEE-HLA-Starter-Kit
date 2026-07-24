package org.see.skf.core;

import java.io.File;

public class TestSEEFederate extends SEEFederate {
    private static final File confFile = new File("src/test/resources/test.conf");

    protected TestSEEFederate(File configurationFile, String... requiredObjects) {
        super(configurationFile, requiredObjects);
    }

    @Override
    protected void declareClasses() {
        try {
            publishObjectClass("HLAobjectRoot.PhysicalEntity",
                    "name", "type", "status", "parent_reference_frame",
                    "state", "acceleration", "rotational_acceleration", "center_of_mass", "body_wrt_structural");

            subscribeObjectClass("HLAobjectRoot.PhysicalEntity",
                    "name", "type", "status", "parent_reference_frame");

            publishObjectClass("HLAobjectRoot.PhysicalEntity.DynamicalEntity",
                    "name", "type", "status", "parent_reference_frame",
                    "state", "acceleration", "rotational_acceleration", "center_of_mass", "body_wrt_structural",
                    "force", "torque", "mass", "mass_rate", "inertia", "inertia_rate");

            subscribeObjectClass("HLAobjectRoot.PhysicalEntity.DynamicalEntity",
                    "name", "type", "status", "parent_reference_frame",
                    "force", "torque", "mass", "mass_rate", "inertia", "inertia_rate");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void declareObjectInstances() {

    }

    @Override
    protected void update() {

    }

    public static void main(String[] args) {
        TestSEEFederate federate = new TestSEEFederate(confFile);

        while (true) {}
    }
}
