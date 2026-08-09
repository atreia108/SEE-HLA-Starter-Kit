package org.see.skf.internal.runtime;

import hla.rti1516_2025.AttributeHandleValueMap;
import hla.rti1516_2025.ObjectClassHandle;
import hla.rti1516_2025.ObjectInstanceHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.HLAClassDeclarationException;
import org.see.skf.core.HLAUtilityFactory;
import org.see.skf.core.ObjectInstanceListener;
import org.see.skf.internal.callbacks.HLACallbackManager;
import org.see.skf.internal.callbacks.NameReservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Predicate;

public final class HLAObjectManager {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectManager.class);

    private final RTIambassador rtiAmbassador;

    private final ExecutorService executor;
    private final HLACallbackManager callbackManager;
    private final SKAnnotatedTypeParser parser;

    private final Set<HLAObjectInstance> objectInstances;
    private final Set<HLAObjectClass> objectClasses;
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

    public void publishObjectClass(String name, String... attributeNames) throws HLAClassDeclarationException {
        if (attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute is required to publish the object class <" + name + ">.");
        }

        HLAObjectClass objectClass = getAndCreateObjectClassIfAbsent(name);
        objectClass.publishAttributes(attributeNames);

        this.objectClasses.add(objectClass);
    }

    // TODO
    public void unpublishObjectClass(String name, String... attributes) {

    }

    public void subscribeObjectClass(String name, String... attributeNames) throws HLAClassDeclarationException {
        if (attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute is required to subscribe the object class <" + name + ">.");
        }

        HLAObjectClass objectClass = getAndCreateObjectClassIfAbsent(name);
        objectClass.subscribeAttributes(attributeNames);

        this.objectClasses.add(objectClass);
    }

    // TODO
    public void unsubscribeObjectClass(String name, String... attributes) {

    }

    private HLAObjectClass getAndCreateObjectClassIfAbsent(String name) {
        HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getName().equals(name));

        if (objectClass == null) {
            objectClass = new HLAObjectClass(name);
        }

        this.objectClasses.add(objectClass);
        return objectClass;
    }

    private HLAObjectClass getObjectClass(Predicate<HLAObjectClass> predicate) {
        return this.objectClasses.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    public void registerObjectInstance(Object object, String name) throws ObjectInstanceCreationException {
        if (getObjectInstance(objInstance -> objInstance.getInstance() != null && objInstance.getName().equals(name)) == null) {
            SKAnnotatedTypeParser.ParsedStructure objectMetadata = this.parser.parseObjectInstance(object);
            String objectClassName = objectMetadata.getClassNameInFom();
            Set<SKAnnotatedTypeParser.Trait> attributes = objectMetadata.getTraits();

            HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getName().equals(objectClassName));

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
            } catch (FederateNotExecutionMember | RestoreInProgress | NotConnected | RTIinternalError | SaveInProgress |
                     ObjectClassNotPublished | ObjectClassNotDefined | ObjectInstanceNameInUse |
                     ObjectInstanceNameNotReserved e) {
                throw new ObjectInstanceCreationException("Could not register <" + object + "> as object instance with RTI.", e);
            } catch (ObjectInstanceNotKnown e) {
                throw new ObjectInstanceCreationException("Name of newly-created object instance with the handle <" + instanceHandle + "> could not be retrieved from RTI.", e);
            }

            HLAObjectInstance objectInstance = new HLAObjectInstance.Builder()
                    .withName(objectInstanceName)
                    .withObjectClass(objectClass)
                    .withHandle(instanceHandle)
                    .withAttributes(attributes)
                    .forObject(object)
                    .build();

            this.objectInstances.add(objectInstance);
            logger.info("Object instance <{}> created.", objectInstanceName);
        } else {
            logger.warn("Attempt made to create an object instance with the name <{}> that already exists.", name);
        }
    }

    public void updateAttributeValues(Object object, String... attributeNames) throws ObjectInstanceUpdateException {
        HLAObjectInstance objectInstance = getObjectInstance(objInstance -> objInstance.getInstance() != null && objInstance.getInstance().equals(object));

        if (objectInstance == null) {
            throw new ObjectInstanceUpdateException("No HLA object instance is associated with the provided object <" + object + ">.");
        }

        objectInstance.updateAttributes(attributeNames);
    }

    public HLAObjectInstance getObjectInstance(Predicate<HLAObjectInstance> predicate) {
        return this.objectInstances.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    boolean reserveName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name for object instance cannot be null.");
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
        HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getHandle().equals(classHandle));

        if (objectClass != null) {
            HLAObjectInstance objectInstance = new HLAObjectInstance.Builder()
                    .withName(name)
                    .withHandle(instanceHandle)
                    .withObjectClass(objectClass)
                    .build();

            AttributeHandleValueMap attributeHandleValueMap;
            try {
                Future<AttributeHandleValueMap> task = this.callbackManager.invokeReflectAttributeValueCallback(objectInstance, objectClass.getSubscribedAttributeHandles());
                attributeHandleValueMap = task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Took too long to receive the latest attribute information for the discovered object instance <" + objectInstance.getName() + ">", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Unexpected exception was thrown while waiting for the latest attribute information of the object instance <" + objectInstance.getName() + ">.", e);
            }

            objectInstance.setCachedAttributeHandleValueMap(attributeHandleValueMap);
            // With the latest attribute values acquired, this object instance is ready for internal use by the federate.
            this.objectInstances.add(objectInstance);
            logger.debug("Discovery process complete for the object instance <{}>. It is now available for use by the federate.", name);
        } else {
            logger.warn("Discovery for the remote object instance <{}> failed to complete as its corresponding object class could not be found. The instance will not available to the federate for use.", name);
        }
    }

    public void remoteObjectInstanceUpdate(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributeValues) {
        Predicate<HLAObjectInstance> predicate = objectInstance -> objectInstance.getHandle().equals(instanceHandle);
        HLAObjectInstance objectInstance = getObjectInstance(predicate);

        if (objectInstance != null) {
            objectInstance.setCachedAttributeHandleValueMap(attributeValues);
        } else {
            logger.warn("Updated values for the object instance with the handle <{}> could not be processed.", instanceHandle);
        }
    }

    public <T> Future<T> remoteObjectInstanceQuery(T object, String name) {
        Predicate<HLAObjectInstance> predicate = objectInstance -> objectInstance.getName().equals(name);

        FutureTask<T> task = new FutureTask<>(() -> {
            while (true) {
                HLAObjectInstance objectInstance = getObjectInstance(predicate);
                if (objectInstance != null) {
                    SKAnnotatedTypeParser.ParsedStructure objectMetadata = this.parser.parseObjectInstance(object);
                    Set<SKAnnotatedTypeParser.Trait> traits = objectMetadata.getTraits();

                    objectInstance.setTraits(traits);
                    objectInstance.setInstance(object);

                    return object;
                }
            }
        });

        this.executor.submit(task);
        return task;
    }

    public void addObjectInstanceListener(ObjectInstanceListener listener) {
        this.objectInstanceListeners.add(listener);
    }

    public void removeObjectInstanceListener(ObjectInstanceListener listener) {
        this.objectInstanceListeners.remove(listener);
    }

    public void addPropertyChangeListener(Object object, String propertyName, PropertyChangeListener listener) {
        HLAObjectInstance objectInstance = getObjectInstance(objInstance -> objInstance.getInstance() != null && objInstance.getInstance().equals(object));

        if (objectInstance != null) {
            objectInstance.addPropertyChangeListener(propertyName, listener);
        } else {
            logger.warn("Cannot add property change listener to object <{}> which is not a registered HLA object instance.", object);
        }
    }

    public void removePropertyChangeListener(Object object, String propertyName, PropertyChangeListener listener) {
        HLAObjectInstance objectInstance = getObjectInstance(objInstance -> objInstance.getInstance().equals(object));

        if (objectInstance != null) {
            objectInstance.removePropertyChangeListener(propertyName, listener);
        } else {
            logger.warn("Cannot remove property change listener from object <{}> which is not a registered HLA object instance.", object);
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
