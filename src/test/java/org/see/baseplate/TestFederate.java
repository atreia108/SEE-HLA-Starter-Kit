package org.see.baseplate;

import hla.rti1516_2025.exceptions.*;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.see.baseplate.encoding.SpaceTimeCoordinateState;
import org.see.baseplate.models.PhysicalEntity;
import org.see.baseplate.models.PhysicalInterface;
import org.see.baseplate.models.ReferenceFrame;
import org.see.skf.core.SEEFederate;

import java.io.File;

public final class TestFederate extends SEEFederate {

    private static final File confFile = new File("src/test/resources/confs/valid.conf");

    private PhysicalEntity spaceport;
    private boolean updated = false;

    TestFederate(File configurationFile, String... requiredObjectNames) {
        super(configurationFile, requiredObjectNames);
    }

    @Override
    protected void declareClasses() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        publishObjectClass(PhysicalEntity.class, "name", "type", "status", "state");
        publishObjectClass(PhysicalInterface.class, "name", "parent_name");
    }

    @Override
    protected void declareObjectInstances() throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.spaceport = new PhysicalEntity();
        this.spaceport.setAcceleration(Vector3D.of(1, 1, 1));
        try {
            createObjectInstance("HLAobjectRoot.PhysicalEntity", "brunel_spaceport", this.spaceport).get();
            createObjectInstance("HLAobjectRoot.PhysicalInterface", "brunel_spaceport_arm", new PhysicalInterface()).get();
            createObjectInstance("HLAobjectRoot.PhysicalInterface", "brunel_spaceport_leg_1", new PhysicalInterface()).get();
            createObjectInstance("HLAobjectRoot.PhysicalInterface", "brunel_spaceport_leg_2", new PhysicalInterface()).get();
            createObjectInstance("HLAobjectRoot.PhysicalInterface", "brunel_spaceport_leg_3", new PhysicalInterface()).get();
            createObjectInstance("HLAobjectRoot.PhysicalInterface", "brunel_spaceport_leg_4", new PhysicalInterface()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processRunJobs() throws RTIexception {
        updateSpaceport();
    }

    private void updateSpaceport() throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress {
        SpaceTimeCoordinateState state = this.spaceport.getState();
        Vector3D pos = state.getPosition();
        Vector3D newPos = pos.add(this.spaceport.getAcceleration());
        state.setPosition(newPos);

        if (!updated) {
            updateObjectInstance(this.spaceport, "state");
            updated = true;
        }
    }

    @Override
    public void processShutdownJobs() {

    }

    public static void main(String[] args) throws RTIexception {
        new TestFederate(confFile)
                .configureAndStart();
    }
}
