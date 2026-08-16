package org.see.skf.internal.runtime;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.internal.HLAUtilityFactory;
import org.see.skf.core.ObjectInstanceListener;
import org.see.skf.internal.callbacks.HLACallbackManager;
import org.see.skf.internal.callbacks.NameReservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Predicate;

public final class HLAObjectManager {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectManager.class);

    private final RTIambassador rtiAmbassador;

    private final ExecutorService executor;
    private final HLACallbackManager callbackManager;
    private final SKAnnotatedTypeParser parser;

    private final Set<HLAObjectClass> objectClasses;
    private final Set<HLAObjectInstance> objectInstances;
    private final Set<ObjectInstanceListener> objectInstanceListeners;

    private HLAObjectManager(Builder builder) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.callbackManager = builder.callbackManager;
        this.parser = builder.parser;
        this.executor = builder.executor;

        this.objectInstances = new CopyOnWriteArraySet<>();
        this.objectClasses = new CopyOnWriteArraySet<>();
        this.objectInstanceListeners = new CopyOnWriteArraySet<>();
    }

    private HLAObjectClass createHLAObjectClass(String name) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        try {
            ObjectClassHandle classHandle = rtiAmbassador.getObjectClassHandle(name);
            AttributeHandleSet emptyAttributeHandleSet = rtiAmbassador.getAttributeHandleSetFactory().create();

            return new HLAObjectClass(name, classHandle, emptyAttributeHandleSet);
        } catch (NameNotFound e) {
            throw new RtiHandleAcquisitionException("<" + name + "> is not a valid object class in the FOM for this federation execution.", e);
        }
    }

    private Map<String, AttributeHandle> createAttributeToHandleMap(HLAObjectClass objectClass, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        Map<String, AttributeHandle> map = new HashMap<>();

        for (String attributeName : attributeNames) {
            try {
                AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(objectClass.getHandle(), attributeName);
                map.put(attributeName, attributeHandle);
            } catch (NameNotFound e) {
                throw new RtiHandleAcquisitionException("<" + attributeName + "> is not a recognized attribute for the object class <" + objectClass.getName() + ">.");
            } catch (InvalidObjectClassHandle e) {
                throw new RtiHandleAcquisitionException(e);
            }
        }

        return map;
    }

    public void publishObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress {
        HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getName().equals(name));
        if (objectClass == null) {
            objectClass = createHLAObjectClass(name);
            this.objectClasses.add(objectClass);
        }

        Map<String, AttributeHandle> attributeToHandle = createAttributeToHandleMap(objectClass, attributeNames);
        objectClass.publishAttributes(attributeToHandle);
    }

    // TODO
    public void unpublishObjectClass(String name, String... attributes) {

    }

    public void subscribeObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress {
        HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getName().equals(name));
        if (objectClass == null) {
            objectClass = createHLAObjectClass(name);
            this.objectClasses.add(objectClass);
        }

        Map<String, AttributeHandle> attributeToHandle = createAttributeToHandleMap(objectClass, attributeNames);
        objectClass.subscribeAttributes(attributeToHandle);
    }

    // TODO
    public void unsubscribeObjectClass(String name, String... attributes) {

    }

    private HLAObjectClass getObjectClass(Predicate<HLAObjectClass> predicate) {
        return this.objectClasses.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    public String registerObjectInstance(Object object) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (object == null) {
            throw new IllegalArgumentException("Cannot create HLA object instance with a NULL object.");
        }

        SKAnnotatedTypeParser.ParsedStructure objectMetadata = this.parser.parseObjectInstance(object);
        String objectClassName = objectMetadata.getClassNameInFom();
        Set<SKAnnotatedTypeParser.Trait> attributes = objectMetadata.getTraits();

        HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getName().equals(objectClassName));
        if (objectClass == null) {
            throw new ObjectInstanceCreationException("Object class <" + objectClassName + "> is unknown. It may not have been previously published/subscribed by this federate.");
        }

        ObjectClassHandle objectClassHandle = objectClass.getHandle();
        ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(objectClassHandle);

        String objectInstanceName;
        try {
            objectInstanceName = rtiAmbassador.getObjectInstanceName(instanceHandle);
        } catch (ObjectInstanceNotKnown e) {
            throw new ObjectInstanceCreationException("Name of newly-created HLA object instance with the handle <" + instanceHandle + "> could not be retrieved from RTI.", e);
        }

        HLAObjectInstance objectInstance = new HLAObjectInstance.Builder()
                .withName(objectInstanceName)
                .withObjectClass(objectClass)
                .withHandle(instanceHandle)
                .withAttributes(attributes)
                .forObject(object)
                .build();

        this.objectInstances.add(objectInstance);
        logger.info("HLA object instance <{}> created.", objectInstanceName);

        return objectInstanceName;
    }

    public Future<Void> registerObjectInstance(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot create an HLA object instance with a NULL object.");
        }

        FutureTask<Void> task = new FutureTask<>(() -> {
            if (getObjectInstance(objInstance -> objInstance.getName().equals(name) && objInstance.getInstance() != null) == null) {
                SKAnnotatedTypeParser.ParsedStructure objectMetadata = this.parser.parseObjectInstance(object);
                String objectClassName = objectMetadata.getClassNameInFom();
                Set<SKAnnotatedTypeParser.Trait> attributes = objectMetadata.getTraits();

                HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getName().equals(objectClassName));
                if (objectClass == null) {
                    throw new ObjectInstanceCreationException("Object class <" + objectClassName + "> is unknown. It may not have been previously published/subscribed by this federate.");
                }
                ObjectClassHandle objectClassHandle = objectClass.getHandle();

                if (!reserveName(name)) {
                    throw new ObjectInstanceCreationException("Unable to register HLA object instance with the name <" + name + ">.");
                }

                ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(objectClassHandle, name);

                HLAObjectInstance objectInstance = new HLAObjectInstance.Builder()
                        .withName(name)
                        .withObjectClass(objectClass)
                        .withHandle(instanceHandle)
                        .withAttributes(attributes)
                        .forObject(object)
                        .build();

                this.objectInstances.add(objectInstance);
                logger.info("Object instance with assigned name <{}> created.", name);
            } else {
                logger.warn("Attempt made to create an HLA object instance with the name <{}> that already exists.", name);
            }

            return null;
        });

        this.executor.submit(task);
        return task;
    }

    public void updateAttributeValues(Object object, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, AttributeNotOwned {
        HLAObjectInstance objectInstance = getObjectInstance(objInstance -> objInstance.getInstance() != null && objInstance.getInstance().equals(object));

        if (objectInstance == null) {
            throw new ObjectInstanceUpdateException("No HLA object instance is associated with the provided object <" + object + ">.");
        }

        AttributeHandleValueMap attributeHandleValueMap = rtiAmbassador.getAttributeHandleValueMapFactory().create(attributeNames.length);
        objectInstance.updateAttributes(attributeHandleValueMap, attributeNames);
    }

    private HLAObjectInstance getObjectInstance(Predicate<HLAObjectInstance> predicate) {
        return this.objectInstances.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    private boolean reserveName(String name) throws FederateNotExecutionMember, RestoreInProgress, IllegalName, NotConnected, RTIinternalError, SaveInProgress {
        if (name == null) {
            throw new IllegalArgumentException("Cannot reserve a name that is NULL.");
        }

        Future<Boolean> task = this.callbackManager.invokeNameReservationCallback(name);
        try {
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NameReservationException("Took too long for name reservation of the object instance <" + name + "> to be completed by the RTI.", e);
        } catch (ExecutionException e) {
            throw new NameReservationException("Unexpected exception was thrown while attempting to reserve the object instance name <" + name + ">.", e);
        }
    }

    public void remoteObjectInstanceDiscovered(ObjectInstanceHandle instanceHandle, String name, ObjectClassHandle classHandle) {
        Runnable r = () -> {
            HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getHandle().equals(classHandle));

            if (objectClass != null) {
                HLAObjectInstance objectInstance = new HLAObjectInstance.Builder()
                        .withName(name)
                        .withHandle(instanceHandle)
                        .withObjectClass(objectClass)
                        .build();

                this.objectInstances.add(objectInstance);
                logger.debug("Discovered the object instance <{}>.", name);

                this.objectInstanceListeners.forEach(listener -> listener.added(name));
            }
        };

        this.executor.submit(r);
    }

    public void remoteObjectInstanceUpdateReceived(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributeValues) {
        HLAObjectInstance objectInstance = getObjectInstance(objInstance -> objInstance.getHandle().equals(instanceHandle));

        if (objectInstance != null) {
            objectInstance.deserialize(attributeValues);
        } else {
            logger.warn("Updated values for the object instance with the handle <{}> could not be processed.", instanceHandle);
        }
    }

    private AttributeHandleValueMap getLatestInstanceAttributeValues(HLAObjectInstance objectInstance) {
        AttributeHandleValueMap attributeHandleValueMap;
        HLAObjectClass objectClass = objectInstance.getObjectClass();

        try {
            Future<AttributeHandleValueMap> task = this.callbackManager.invokeReflectAttributeValueCallback(objectInstance, objectClass.getSubscribedAttributeHandles());
            attributeHandleValueMap = task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Took too long to receive the latest attribute information for the discovered object instance <" + objectInstance.getName() + ">", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Unexpected exception was thrown while waiting for the latest attribute information of the object instance <" + objectInstance.getName() + ">.", e);
        }

        return attributeHandleValueMap;
    }

    private void makeObjectInstanceTrackable(HLAObjectInstance objectInstance, Object object) {
        if (objectInstance != null && object != null) {
            SKAnnotatedTypeParser.ParsedStructure objectMetadata = this.parser.parseObjectInstance(object);
            Set<SKAnnotatedTypeParser.Trait> traits = objectMetadata.getTraits();

            objectInstance.makeTrackable(object, traits);
        }
    }

    // TODO
    public void remoteObjectInstanceDeleted() {

    }

    public <T> Future<T> launchRemoteObjectInstanceQuery(T object, String name) {
        FutureTask<T> operation = new FutureTask<>(() -> {
            Predicate<HLAObjectInstance> predicate = objInstance -> objInstance.getName().equals(name);
            HLAObjectInstance objectInstance = getObjectInstance(predicate);

            if (objectInstance != null && objectInstance.isTrackable() ) {
                // Disallow swapping of the object assigned to HLAObjectInstance.instance field.
                if (!objectInstance.getInstance().equals(object)) {
                    throw new IllegalArgumentException("Data for the object instance <" + name + "> is already being written to another object than the one supplied as argument.");
                } else {
                    return object;
                }
            } else {
                while (true) {
                    if ((objectInstance = getObjectInstance(predicate)) != null) {
                        break;
                    }
                }
            }

            makeObjectInstanceTrackable(objectInstance, object);
            AttributeHandleValueMap attributeHandleValueMap = getLatestInstanceAttributeValues(objectInstance);
            objectInstance.deserialize(attributeHandleValueMap);

            return object;
        });

        this.executor.submit(operation);
        return operation;
    }

    public Object queryObjectInstance(String name) {
        HLAObjectInstance objectInstance = getObjectInstance(objInstance -> objInstance.getName().equals(name) && objInstance.getInstance() != null);

        if (objectInstance != null) {
            return objectInstance.getInstance();
        } else {
            return null;
        }
    }

    public void addObjectInstanceListener(ObjectInstanceListener objectInstanceListener) {
        if (objectInstanceListener != null) {
            this.objectInstanceListeners.add(objectInstanceListener);
        }
    }

    public void removeObjectInstanceListener(ObjectInstanceListener objectInstanceListener) {
        if (objectInstanceListener != null) {
            this.objectInstanceListeners.remove(objectInstanceListener);
        }
    }

    public void addPropertyChangeListener(Object object, String propertyName, PropertyChangeListener listener) {
        if (object == null) {
            return;
        }

        HLAObjectInstance objectInstance = getObjectInstance(objInstance -> objInstance.getInstance() != null && objInstance.getInstance().equals(object));
        if (objectInstance != null) {
            objectInstance.addPropertyChangeListener(propertyName, listener);
        }
    }

    public void removePropertyChangeListener(Object object, String propertyName, PropertyChangeListener listener) {
        if (object == null) {
            return;
        }

        HLAObjectInstance objectInstance = getObjectInstance(objInstance -> objInstance.getInstance().equals(object));
        if (objectInstance != null) {
            objectInstance.removePropertyChangeListener(propertyName, listener);
        }
    }

    public static final class Builder {

        private HLACallbackManager callbackManager;
        private SKAnnotatedTypeParser parser;
        private ExecutorService executor;

        public Builder callbackManager(HLACallbackManager callbackManager) {
            this.callbackManager = callbackManager;
            return this;
        }

        public Builder parser(SKAnnotatedTypeParser parser) {
            this.parser = parser;
            return this;
        }

        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public HLAObjectManager build() {
            if (callbackManager == null || parser == null || executor == null) {
                throw new IllegalStateException("One or more objects required to initialize HLAObjectManager are missing.");
            }

            return new HLAObjectManager(this);
        }
    }
}
