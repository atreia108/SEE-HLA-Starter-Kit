package org.see.skf.internal.runtime;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.ObjectInstanceListener;
import org.see.skf.internal.HLAUtilityFactory;
import org.see.skf.internal.callbacks.HLACallbackManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class HLAObjectManager2 {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectManager2.class);

    private final RTIambassador rtiAmbassador;

    private final ExecutorService executor;

    private final HLACallbackManager callbackManager;

    private final SKAnnotatedTypeParser2 parser;

    private final Set<HLAObjectClass2> objectClasses;

    private final Set<ObjectInstance> objectInstances;

    private final Set<ObjectInstanceListener> objectInstanceListeners;

    private final Set<ObjectInstanceHandle> instancesPendingInitialValues;

    public HLAObjectManager2(HLACallbackManager callbackManager, ExecutorService executor, SKAnnotatedTypeParser2 parser) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.executor = executor;
        this.callbackManager = callbackManager;
        this.parser = parser;

        this.objectClasses = new CopyOnWriteArraySet<>();
        this.objectInstances = new CopyOnWriteArraySet<>();
        this.objectInstanceListeners = new CopyOnWriteArraySet<>();
        this.instancesPendingInitialValues = new CopyOnWriteArraySet<>();
    }

    private HLAObjectClass2 createObjectClass(Class<?> proxyClass) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        SKAnnotatedTypeParser2.Metadata proxyMetadata = this.parser.parseObjectInstanceProxy(proxyClass);
        String className = proxyMetadata.getFomClassName();

        ObjectClassHandle handle;
        try {
            handle = rtiAmbassador.getObjectClassHandle(className);
        } catch (NameNotFound e) {
            throw new IllegalArgumentException("The HLA object class <" + className + "> is not defined in any FOM modules currently being used in the federation execution.");
        }

        return new HLAObjectClass2.Builder()
                .withHandle(handle)
                .withMetadata(proxyMetadata)
                .build();
    }

    public void publishObjectClass(Class<?> proxyClass, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress {
        if (proxyClass == null) {
            throw new IllegalArgumentException("Class representing how instances of the HLA object class should be interpreted by the federate cannot be NULL.");
        } else if (attributeNames == null || attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute has to be provided for the object class to be published.");
        }

        HLAObjectClass2 objectClass;
        if ((objectClass = getObjectClass(objClass -> objClass.getProxyClass().equals(proxyClass))) == null) {
            objectClass = createObjectClass(proxyClass);
            this.objectClasses.add(objectClass);
        }

        objectClass.publishAttributes(attributeNames);
    }

    public void unpublishObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress {
        HLAObjectClass2 objectClass;
        if ((objectClass = getObjectClass(objClass -> objClass.getName().equals(name))) != null) {
            objectClass.unpublishAttributes(attributeNames);
        }
    }

    public void subscribeObjectClass(Class<?> proxyClass, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress {
        if (proxyClass == null) {
            throw new IllegalArgumentException("Class representing how instances of the HLA object class should be interpreted by the federate cannot be NULL.");
        } else if (attributeNames == null || attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute has to be provided for the object class to be subscribed.");
        }

        HLAObjectClass2 objectClass;
        if ((objectClass = getObjectClass(objClass -> objClass.getProxyClass().equals(proxyClass))) == null) {
            objectClass = createObjectClass(proxyClass);
            this.objectClasses.add(objectClass);
        }

        objectClass.subscribeAttributes(attributeNames);
    }

    public void unsubscribeObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        HLAObjectClass2 objectClass;
        if ((objectClass = getObjectClass(objClass -> objClass.getName().equals(name))) != null) {
            objectClass.unsubscribeAttributes(attributeNames);
        }
    }

    private HLAObjectClass2 getObjectClass(Predicate<HLAObjectClass2> predicate) {
        return this.objectClasses.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    private ObjectInstance getObjectInstance(Predicate<ObjectInstance> predicate) {
        return this.objectInstances.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    // TODO
    void createObjectInstance(String name, Object proxy) {

    }

    // TODO
    void createObjectInstance(Object proxy) {

    }

    // TODO
    void updateObjectInstance(Object proxy, String... attributeNames) {

    }

    // TODO
    void destroyObjectInstance(Object proxy) {

    }

    public void remoteObjectInstanceDiscovered(ObjectInstanceHandle instanceHandle, String instanceName, ObjectClassHandle classHandle) {
        HLAObjectClass2 objectClass = getObjectClass(objClass -> objClass.getHandle().equals(classHandle));

        if (objectClass != null) {
            Object proxy = objectClass.createProxy();
            ObjectInstance instance = new ObjectInstance(instanceName, instanceHandle, proxy, objectClass);

            this.objectInstances.add(instance);
            this.instancesPendingInitialValues.add(instanceHandle);

            try {
                requestAttributeValueUpdate(instanceName, instanceHandle, objectClass.getSubscribedAttributes());
            } catch (RTIexception e) {
                logger.error("Failed to request latest attribute values for the newly-discovered object instance <{}>", instanceName);
            }
            // this.callbackManager.invokeInstanceDiscoveryValueAcquisitionCallback(instanceName, instanceHandle, objectClass.getSubscribedAttributes());

        } else {
            logger.debug("Ignored newly-discovered object instance <{}> as no corresponding object class information for it is known.", instanceName);
        }
    }

    public void remoteObjectInstanceUpdated(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributeValues, String producingFederateName) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.handle.equals(instanceHandle));

        if (objectInstance != null) {
            HLAObjectClass2 objectClass = objectInstance.objectClass;
            objectClass.reflectRemoteUpdate(objectInstance, attributeValues);

            if (this.instancesPendingInitialValues.contains(instanceHandle)) {
                this.objectInstanceListeners.forEach(listener -> listener.created(objectInstance.name, objectInstance.proxy, producingFederateName));
            }

            /*
            if (broadcastInstanceDiscovery) {
                this.objectInstanceListeners.forEach(listener -> listener.created(objectInstance.name, objectInstance.proxy, producingFederateName));
            }
             */
        } else {
            logger.error("Discarded update received for object instance with the handle <{}> as no corresponding serialization information for it is known to the federate.", instanceHandle);
        }
    }

    public void remoteObjectInstanceDestroyed(ObjectInstanceHandle instanceHandle, String producingFederateName) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.handle.equals(instanceHandle));

        if (objectInstance != null) {
            this.objectInstances.remove(objectInstance);
            this.objectInstanceListeners.forEach(listener -> listener.destroyed(objectInstance.name, producingFederateName));
        }
    }

    public Object queryObjectInstance(String instanceName) {
        for (ObjectInstance instance : this.objectInstances) {
            if (instance.name.equals(instanceName)) {
                return instance.proxy;
            }
        }

        return null;
    }

    private void requestAttributeValueUpdate(String name, ObjectInstanceHandle handle, AttributeHandleSet attributes) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        try {
            rtiAmbassador.requestAttributeValueUpdate(handle, attributes, null);
        } catch (AttributeNotDefined | ObjectInstanceNotKnown e) {
            throw new RuntimeException("Failed to request the latest attribute values for the object instance <" + name + ">.", e);
        }
    }

    public void requestRemoteObjectInstanceUpdates(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.name.equals(name));

        if (objectInstance != null) {
            AttributeHandleSet set = objectInstance.objectClass.getAttributeHandlesAsSet(attributeNames);
            requestAttributeValueUpdate(name, objectInstance.handle, set);
        } else {
            logger.warn("Cannot request the latest the values for the remote object instance <{}> because it has not been discovered by this federate yet.", name);
        }
    }

    public void addObjectInstanceListener(ObjectInstanceListener listener) {
        this.objectInstanceListeners.add(listener);
    }

    public void removeObjectInstanceListener(ObjectInstanceListener listener) {
        this.objectInstanceListeners.remove(listener);
    }

    public void addPropertyChangeListener(Object proxy, PropertyChangeListener listener) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));

        if (objectInstance != null) {
            objectInstance.pcs.addPropertyChangeListener(listener);
        }
    }

    public void addPropertyChangeListener(Object proxy, String propertyName, PropertyChangeListener listener) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));

        if (objectInstance != null) {
            objectInstance.pcs.addPropertyChangeListener(propertyName, listener);
        }
    }

    public void removePropertyChangeListener(Object proxy, PropertyChangeListener listener) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));

        if (objectInstance != null) {
            objectInstance.pcs.removePropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(Object proxy, String propertyName, PropertyChangeListener listener) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));

        if (objectInstance != null) {
            objectInstance.pcs.removePropertyChangeListener(propertyName, listener);
        }
    }

    public static final class ObjectInstance {

        private final HLAObjectClass2 objectClass;

        private final String name;

        private final ObjectInstanceHandle handle;

        private final Object proxy;

        private final PropertyChangeSupport pcs;

        private ObjectInstance(String name, ObjectInstanceHandle handle, Object proxy, HLAObjectClass2 objectClass) {
            this.name = name;
            this.handle = handle;
            this.proxy = proxy;
            this.objectClass = objectClass;

            this.pcs = new PropertyChangeSupport(this);
        }

        Object getProxy() {
            return this.proxy;
        }

        void notifyAllListeners(String propertyName, Object oldValue, Object newValue) {
            this.pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
}
