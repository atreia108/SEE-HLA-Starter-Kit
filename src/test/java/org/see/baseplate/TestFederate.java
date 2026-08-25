package org.see.baseplate;

import hla.rti1516_2025.exceptions.*;
import org.see.baseplate.models.PhysicalEntity;
import org.see.baseplate.models.ReferenceFrame;
import org.see.skf.core.SEEFederate;

import java.io.File;

public final class TestFederate extends SEEFederate {

    private static File confFile = new File("src/test/resources/confs/valid.conf");

    private PhysicalEntity spaceport;
    private int counter = 0;

    TestFederate(File configurationFile, String... requiredObjectNames) {
        super(configurationFile, requiredObjectNames);
    }

    @Override
    protected void declareClasses() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, FederateServiceInvocationsAreBeingReportedViaMOM {
        publishObjectClass(PhysicalEntity.class, "name", "type", "status", "state");
        subscribeObjectClass(ReferenceFrame.class, "name", "parent_name", "state");
    }

    @Override
    protected void declareObjectInstances() {
        this.spaceport = new PhysicalEntity();
        createObjectInstance("HLAobjectRoot.PhysicalEntity", "brunel_spaceport", this.spaceport);
    }

    @Override
    public void processRunJobs() throws RTIexception {

        if (counter > 5) {
            destroyObjectInstance(this.spaceport);
            shutdownExecution();
        }

        ++counter;
    }

    @Override
    public void processShutdownJobs() {

    }

    public static void main(String[] args) throws RTIexception {
        new TestFederate(confFile)
                .configureAndStart();
    }
}
