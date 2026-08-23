package org.see.baseplate;

import hla.rti1516_2025.exceptions.*;
import org.see.baseplate.models.ReferenceFrame;
import org.see.skf.core.SEEFederate;

import java.io.File;

public final class TestFederate extends SEEFederate {

    private static File confFile = new File("src/test/resources/confs/valid.conf");

    TestFederate(File configurationFile, String... requiredObjectNames) {
        super(configurationFile, requiredObjectNames);
    }

    @Override
    protected void declareClasses() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, FederateServiceInvocationsAreBeingReportedViaMOM {
        subscribeObjectClass(ReferenceFrame.class, "name", "parent_name", "state");
    }

    @Override
    protected void declareObjectInstances() {

    }

    @Override
    public void processRunJobs() throws RTIexception {

    }

    @Override
    public void processShutdownJobs() throws RTIexception {

    }

    public static void main(String[] args) throws RTIexception {
        new TestFederate(confFile)
                .configureAndStart();
    }
}
