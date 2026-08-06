package org.see.skf.core;

import hla.rti1516_2025.ObjectClassHandle;
import hla.rti1516_2025.ObjectInstanceHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.callbacks.HLACallbackManager;
import org.see.skf.runtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

final class HLAObjectManager {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectManager.class);

    private final RTIambassador rtiAmbassador;

    private final Set<HLAObjectInstance> objectInstances;
    private final HLACallbackManager callbackManager;
    private final SKAnnotatedTypeParser parser;
    private final Set<HLAObjectClass> objectClasses;

    HLAObjectManager(HLACallbackManager callbackManager, SKAnnotatedTypeParser parser) {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        this.objectInstances = new CopyOnWriteArraySet<>();
        this.callbackManager = callbackManager;
        this.parser = parser;
        this.objectClasses = new CopyOnWriteArraySet<>();
    }

    void publishObjectClass(String name, String... attributeNames) throws HLAClassDeclarationException {
        if (attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute is required to publish the object class <" + name + ">.");
        }

        HLAObjectClass objectClass = getAndCreateObjectClassIfAbsent(name);
        objectClass.publish(attributeNames);
    }

    void unpublishObjectClass(String name, String... attributes) {

    }

    void subscribeObjectClass(String name, String... attributeNames) throws HLAClassDeclarationException {
        if (attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute is required to subscribe the object class <" + name + ">.");
        }

        HLAObjectClass objectClass = getAndCreateObjectClassIfAbsent(name);
        objectClass.subscribe(attributeNames);
    }

    void unsubscribeObjectClass(String name, String... attributes) {

    }

    private HLAObjectClass getAndCreateObjectClassIfAbsent(String name) {
        HLAObjectClass objectClass = this.objectClasses.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseGet(() -> new HLAObjectClass(name));

        this.objectClasses.add(objectClass);
        return objectClass;
    }

    private HLAObjectClass getObjectClass(String name) {
        return objectClasses.stream()
                .filter(objectClass -> objectClass.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    void registerObjectInstance(Object object, String name) throws ObjectInstanceCreationException {
        SKAnnotatedTypeParser.ParsedStructure objectMetadata = this.parser.parseObjectInstance(object);
        String objectClassName = objectMetadata.getClassNameInFom();
        HLAObjectClass objectClass = getObjectClass(objectClassName);

        if (objectClass == null) {
            throw new ObjectInstanceCreationException("Object class <" + objectClassName + "> is unknown. It may not have been previously published/subscribed by this federate.");
        }

        ObjectClassHandle objectClassHandle = objectClass.getHandle();

        ObjectInstanceHandle instanceHandle = null;
        String objectInstanceName;
        try {
            if (name != null) {
                if (!reserveName(name)) {
                    throw new ObjectInstanceCreationException("Unable to register object instance with the name <" + name + ">.");
                }

                objectInstanceName = name;
                instanceHandle = rtiAmbassador.registerObjectInstance(objectClassHandle, objectInstanceName);
            } else {
                instanceHandle = rtiAmbassador.registerObjectInstance(objectClassHandle);
                objectInstanceName = rtiAmbassador.getObjectInstanceName(instanceHandle);
            }
        }  catch (FederateNotExecutionMember | RestoreInProgress | NotConnected | RTIinternalError | SaveInProgress | ObjectClassNotPublished | ObjectClassNotDefined | ObjectInstanceNameInUse | ObjectInstanceNameNotReserved e) {
            throw new ObjectInstanceCreationException("Could not register <" + object + "> as object instance with RTI.", e);
        } catch (ObjectInstanceNotKnown e) {
            throw new ObjectInstanceCreationException("Name of newly-created object instance with the handle <" + instanceHandle + "> could not be retrieved from RTI.", e);
        }

        Map<SKAnnotatedTypeParser.Trait, Boolean> ownershipMap = new HashMap<>();
        objectMetadata.getTraits().forEach(t -> ownershipMap.put(t, true));

        HLAObjectInstance objectInstance = new HLAObjectInstance.Builder()
                .withName(objectInstanceName)
                .withObjectClass(objectClass)
                .withHandle(instanceHandle)
                .withAttributes(ownershipMap)
                .forObject(object)
                .build();

        this.objectInstances.add(objectInstance);
        logger.info("Object instance <{}> created.", objectInstanceName);
    }

    void updateAttributeValues(Object object, String... attributeNames) throws ObjectInstanceUpdateException {
        HLAObjectInstance objectInstance = getObjectInstance(object);

        if (objectInstance == null) {
            throw new ObjectInstanceUpdateException("No HLA object instance is associated with the provided object <" + object + ">.");
        }

        objectInstance.updateAttributes(attributeNames);
    }

    private HLAObjectInstance getObjectInstance(Object object) {
        return this.objectInstances.stream()
                .filter(instanceWrapper -> instanceWrapper.getInstance().equals(object))
                .findFirst()
                .orElse(null);
    }

    boolean reserveName(String name) throws ObjectInstanceCreationException {
        if (name == null) {
            throw new IllegalArgumentException("Name for object instance cannot be null.");
        }

        Future<Boolean> task = this.callbackManager.invokeNameReservationCallback(name);
        try {
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ObjectInstanceCreationException("Name reservation for object instance <" + name + "> took too long.", e);
        } catch (ExecutionException e) {
            throw new ObjectInstanceCreationException("Unexpected exception was thrown while attempting to reserve the object instance name <" + name + ">.", e);
        }
    }
}
