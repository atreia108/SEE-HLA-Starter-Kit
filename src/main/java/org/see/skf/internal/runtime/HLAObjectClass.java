package org.see.skf.internal.runtime;

import hla.rti1516_2025.AttributeHandle;
import hla.rti1516_2025.AttributeHandleSet;
import hla.rti1516_2025.ObjectClassHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.internal.HLAUtilityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

final class HLAObjectClass {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectClass.class);
    private static final String HLA_PRIVILEGE_TO_DELETE_OBJECT_ATTRIBUTE = "HLAprivilegeToDeleteObject";

    private final RTIambassador rtiAmbassador;

    private final String name;
    private final ObjectClassHandle handle;
    private final Set<Attribute> attributes;
    private final AttributeHandleSet attributeHandleSet;

    HLAObjectClass(String name, ObjectClassHandle handle, AttributeHandleSet emptyAttributeHandleSet) {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.name = name;
        this.handle = handle;
        this.attributeHandleSet = emptyAttributeHandleSet;

        this.attributes = new CopyOnWriteArraySet<>();
    }

    void publishAttributes(Map<String, AttributeHandle> attributeToHandle) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        Set<Attribute> publishedAttributes = new HashSet<>(attributeToHandle.size());

        for (Map.Entry<String, AttributeHandle> entry : attributeToHandle.entrySet()) {
            String attributeName = entry.getKey();
            AttributeHandle attributeHandle = entry.getValue();
            Attribute attribute = getAttribute(name);

            if (attribute == null) {
                attribute = new Attribute(attributeName, attributeHandle);
                this.attributes.add(attribute);
            }

            attribute.published.set(true);
            publishedAttributes.add(attribute);
        }

        this.attributeHandleSet.clear();
        publishedAttributes.forEach(attribute -> this.attributeHandleSet.add(attribute.handle));

        try {
            rtiAmbassador.publishObjectClassAttributes(this.handle, attributeHandleSet);
        } catch (AttributeNotDefined | ObjectClassNotDefined e) {
            throw new RuntimeException(e);
        }

        if (getAttribute(HLA_PRIVILEGE_TO_DELETE_OBJECT_ATTRIBUTE) == null) {
            this.attributes.add(createHLAPrivilegeToDeleteObjectAttribute(this.handle));
        }

        String loggableAttributeNames = getNamesInLoggableFormat(attributeToHandle.keySet());
        logger.info("Published <{}> attributes: {}", this.name, loggableAttributeNames);
    }

    private Attribute createHLAPrivilegeToDeleteObjectAttribute(ObjectClassHandle classHandle) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        AttributeHandle attributeHandle = null;
        try {
            attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, HLA_PRIVILEGE_TO_DELETE_OBJECT_ATTRIBUTE);
        } catch (InvalidObjectClassHandle | NameNotFound e) {
            // In theory, this shouldn't be thrown since, if there is something wrong with the object class handle, an error would've been thrown much earlier.
            // Additionally, every object class inherits from HLAobjectRoot which has HLAprivilegeToDeleteObject as an attribute, so the RTI shouldn't have the chance to complain.
            logger.error("The attribute <{}> was not created for the object class <{}>. Attribute ownership operations will not function correctly.", HLA_PRIVILEGE_TO_DELETE_OBJECT_ATTRIBUTE, this.name, e);
        }

        Attribute privilegeToDeleteObjectAttribute =  new Attribute(HLA_PRIVILEGE_TO_DELETE_OBJECT_ATTRIBUTE, attributeHandle);
        privilegeToDeleteObjectAttribute.published.set(true);

        return privilegeToDeleteObjectAttribute;
    }

    // TODO
    void unpublish(String... attributeNames) {
        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(attributeName);

            if (attribute != null) {
                attribute.published.set(false);
            } else {
                logger.warn("Could not unpublish the attribute <{}> of object class <{}> because it has not been published yet.", attributeName, this.name);
            }
        }

        // TODO - Remember to unpublish the HLAprivilegeToDeleteObject attribute since it's dealt with separately!
    }

    void subscribeAttributes(Map<String, AttributeHandle> attributeToHandle) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        Set<Attribute> subscribedAttributes = new HashSet<>(attributeToHandle.size());

        for (Map.Entry<String, AttributeHandle> entry : attributeToHandle.entrySet()) {
            String attributeName = entry.getKey();
            AttributeHandle attributeHandle = entry.getValue();
            Attribute attribute = getAttribute(attributeName);

            if (attribute == null) {
                attribute = new Attribute(attributeName, attributeHandle);
                this.attributes.add(attribute);
            }

            attribute.subscribed.set(true);
            subscribedAttributes.add(attribute);
        }

        this.attributeHandleSet.clear();
        subscribedAttributes.forEach(attribute -> this.attributeHandleSet.add(attribute.handle));

        String loggableAttributeNames = getNamesInLoggableFormat(attributeToHandle.keySet());
        try {
            rtiAmbassador.subscribeObjectClassAttributes(this.handle, attributeHandleSet);
        } catch (AttributeNotDefined | ObjectClassNotDefined e) {
            throw new RuntimeException(e);
        }

        logger.info("Subscribed <{}> attributes: {}", this.name, loggableAttributeNames);
    }

    // TODO
    void unsubscribe(String... attributeNames) {
        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(attributeName);

            if (attribute != null) {
                attribute.subscribed.set(false);
            } else {
                logger.warn("Could not unsubscribe the attribute <{}> of object class <{}> because it has not been published yet.", attributeName, this.name);
            }
        }
    }

    private String getNamesInLoggableFormat(Set<String> attributeNames) {
        StringBuilder sb = new StringBuilder("[ ");

        String[] attributeNamesArray = attributeNames.toArray(String[]::new);

        for (int i = 0; i < attributeNamesArray.length; i++) {
            sb.append(attributeNamesArray[i]);

            if (i != attributeNamesArray.length - 1) {
                sb.append(", ");
            } else {
                sb.append(" ]");
            }
        }

        return sb.toString();
    }

    String getName() {
        return this.name;
    }

    ObjectClassHandle getHandle() {
        return this.handle;
    }

    Attribute getAttribute(String name) {
        return this.attributes.stream()
                .filter(attribute -> attribute.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    AttributeHandleSet getSubscribedAttributeHandles() {
        this.attributeHandleSet.clear();

        this.attributes.forEach(attribute -> {
            if (attribute.subscribed.get()) {
                this.attributeHandleSet.add(attribute.handle);
            }
        });

        return this.attributeHandleSet;
    }

    static final class Attribute {

        private final String name;

        private final AttributeHandle handle;

        private final AtomicBoolean published;

        private final AtomicBoolean subscribed;

        private Attribute(String name, AttributeHandle handle) {
            this.name = name;
            this.handle = handle;
            this.published = new AtomicBoolean(false);
            this.subscribed = new AtomicBoolean(false);
        }

        String getName() {
            return this.name;
        }

        AttributeHandle getHandle() {
            return this.handle;
        }
    }
}
