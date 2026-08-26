package org.see.baseplate;

import hla.rti1516_2025.exceptions.*;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.see.baseplate.encoding.SpaceTimeCoordinateState;
import org.see.baseplate.models.PhysicalEntity;
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
        subscribeObjectClass(ReferenceFrame.class, "name", "parent_name", "state");
    }

    @Override
    protected void declareObjectInstances() throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.spaceport = new PhysicalEntity();
        this.spaceport.setAcceleration(Vector3D.of(1, 1, 1));
        createObjectInstance("HLAobjectRoot.PhysicalEntity", this.spaceport);
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
