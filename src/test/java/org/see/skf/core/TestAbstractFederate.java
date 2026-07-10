package org.see.skf.core;

import hla.rti1516_2025.*;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.exceptions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TestAbstractFederate extends NullFederateAmbassador {
    protected final ExecutorService executorService;

    protected final RTIambassador rtiAmbassador;
    protected final EncoderFactory encoderFactory;

    private final String rtiAddress;
    private final String federateName;
    private final String federateType;
    private final String federationName;

    protected ObjectClassHandle executionConfigurationClassHandle;
    protected AttributeHandle scenarioTimeEpochAttributeHandle;
    protected AttributeHandle currentExecutionModeAttributeHandle;
    protected AttributeHandle nextExecutionModeAttributeHandle;
    protected AttributeHandle leastCommonTimeStepAttributeHandle;

    protected ObjectClassHandle physicalEntityClassHandle;
    protected AttributeHandle PENameAttributeHandle;
    protected AttributeHandle PEStatusAttributeHandle;
    protected AttributeHandle PEParentReferenceFrameAttributeHandle;
    protected AttributeHandleSet physicalEntityAttributeHandleSet;

    protected ObjectClassHandle dynamicalEntityClassHandle;
    protected AttributeHandle DENameAttributeHandle;
    protected AttributeHandle DETypeAttributeHandle;
    protected AttributeHandle DEStatusAttributeHandle;
    protected AttributeHandle DEParentReferenceFrameAttributeHandle;
    protected AttributeHandleSet dynamicalEntityAttributeHandleSet;

    protected ObjectClassHandle referenceFrameClassHandle;
    protected AttributeHandle RFNameAttributeHandle;
    protected AttributeHandle RFParentNameAttributeHandle;
    protected AttributeHandleSet referenceFrameAttributeHandleSet;

    protected TestAbstractFederate(String rtiAddress, String federateName, String federateType, String federationName) {
        try {
            RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
            this.rtiAmbassador = rtiFactory.getRtiAmbassador();
            this.encoderFactory = rtiFactory.getEncoderFactory();
        } catch (RTIinternalError e) {
            throw new RuntimeException(e);
        }

        this.rtiAddress = rtiAddress;
        this.federateName = federateName;
        this.federateType = federateType;
        this.federationName = federationName;

        this.executorService = Executors.newCachedThreadPool();

        connect();
        join();

        try {
            initHandles();
            declareClasses();
            declareInstances();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        update();
    }

    private void connect() {
        RtiConfiguration rtiConfiguration = RtiConfiguration.createConfiguration().withRtiAddress(rtiAddress);

        try {
            rtiAmbassador.connect(this, CallbackModel.HLA_IMMEDIATE, rtiConfiguration);
        } catch (Unauthorized | RTIinternalError | ConnectionFailed | UnsupportedCallbackModel | AlreadyConnected |
                 CallNotAllowedFromWithinCallback e) {
            throw new RuntimeException(e);
        }
    }

    private void join() {
        try {
            rtiAmbassador.joinFederationExecution(federateName, federateType, federationName);
        } catch (CouldNotCreateLogicalTimeFactory | NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError |
                 FederateNameAlreadyInUse | FederationExecutionDoesNotExist | SaveInProgress | RestoreInProgress |
                 FederateAlreadyExecutionMember | Unauthorized e) {
            throw new RuntimeException(e);
        }
    }

    private void initHandles() throws FederateNotExecutionMember, NotConnected, NameNotFound, RTIinternalError, InvalidObjectClassHandle {
        referenceFrameClassHandle = rtiAmbassador.getObjectClassHandle("HLAobjectRoot.ReferenceFrame");
        referenceFrameAttributeHandleSet = rtiAmbassador.getAttributeHandleSetFactory().create();
        RFNameAttributeHandle = rtiAmbassador.getAttributeHandle(referenceFrameClassHandle, "name");
        RFParentNameAttributeHandle = rtiAmbassador.getAttributeHandle(referenceFrameClassHandle,"parent_name");
        referenceFrameAttributeHandleSet.add(RFNameAttributeHandle);
        referenceFrameAttributeHandleSet.add(RFParentNameAttributeHandle);

        physicalEntityAttributeHandleSet = rtiAmbassador.getAttributeHandleSetFactory().create();

        dynamicalEntityAttributeHandleSet = rtiAmbassador.getAttributeHandleSetFactory().create();
    }

    protected abstract void declareClasses() throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress;

    protected abstract void declareInstances();

    protected abstract void update();

    @Override
    public void informAttributeOwnership(ObjectInstanceHandle objectInstance, AttributeHandleSet attributes, FederateHandle owner) throws FederateInternalError {
        try {
            // String objectName = rtiAmbassador.getObjectInstanceName(objectInstance);
            ObjectClassHandle classHandle = rtiAmbassador.getKnownObjectClassHandle(objectInstance);
            String ownerName = rtiAmbassador.getFederateName(owner);

            for (AttributeHandle attributeHandle : attributes) {
                String attributeName = rtiAmbassador.getAttributeName(classHandle, attributeHandle);
                System.out.println(attributeName + " -> " + ownerName);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
