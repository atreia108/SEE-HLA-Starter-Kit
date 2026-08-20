package org.see.skf.core;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.internal.runtime.models.PhysicalEntity;
import org.see.skf.internal.runtime.models.Vector3;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestSEEFederate extends SEEFederate {
    private static final File confFile = new File("src/test/resources/confs/valid.conf");

    private PhysicalEntity spaceport;

    protected TestSEEFederate(File configurationFile, String... requiredObjects) {
        super(configurationFile, requiredObjects);
    }

    @Override
    protected void declareClasses() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, FederateServiceInvocationsAreBeingReportedViaMOM {
        publishObjectClass("HLAobjectRoot.PhysicalEntity", "acceleration");

        subscribeInteractionClass("HLAinteractionRoot.MSGAttributeTransferAvailable", MSGAttributeTransferAvailable.class);
        InteractionListener listener = (proxy, producingFederate) -> {
            MSGAttributeTransferAvailable interaction = (MSGAttributeTransferAvailable) proxy;
            System.out.println(producingFederate + "says: Attribute ownership is ready!" + "Proprietor: " + interaction.getProprietorFederate() + " Target: " + interaction.getTargetObject());
        };

        addInteractionListener(listener);
    }

    @Override
    protected void declareObjectInstances() {
        this.spaceport = new PhysicalEntity();
        Future<Void> future = createObjectInstance(this.spaceport, "brunel_spaceport");
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
    }

    @Override
    public void processRunJobs() {

        /*
        try {
            Vector3 accel = this.spaceport.getAcceleration();
            this.spaceport.setAcceleration(new Vector3(accel.getX() + 1.0, accel.getY() + 1.0, accel.getZ() + 1.0));
            updateObjectInstance(spaceport, "acceleration");
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
    }

    @Override
    public void processShutdownJobs() {

    }

    public static void main(String[] args) throws RTIexception {
        new TestSEEFederate(confFile).configureAndStart();
    }
}
