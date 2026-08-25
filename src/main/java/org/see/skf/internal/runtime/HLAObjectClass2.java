package org.see.skf.internal.runtime;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.internal.HLAUtilityFactory;
import org.see.skf.internal.InternalObjectBuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

final class HLAObjectClass2 {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectClass2.class);
    // private static final String HLA_PRIVILEGE_TO_DELETE_OBJECT = "HLAprivilegeToDeleteObject";

    private final RTIambassador rtiAmbassador;

    private final ObjectClassHandle handle;

    // private final AttributeHandle privilegeToDeleteObject;

    private final String name;

    private final Class<?> proxyClass;

    private final Map<Attribute, Trait> attributeToTrait;

    private final AttributeHandleValueMap attributeValues;

    private final AttributeHandleSet attributes;

    private HLAObjectClass2(Builder builder) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.handle = builder.handle;
        // this.privilegeToDeleteObject = createHLAPrivilegeToDeleteObjectAttribute();
        this.name = builder.objectMetadata.getFomClassName();
        this.proxyClass = builder.objectMetadata.getProxyClass();
        this.attributeToTrait = computeAttributeToTraitAssociation(builder.objectMetadata.getTraits());
        this.attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        this.attributeValues = rtiAmbassador.getAttributeHandleValueMapFactory().create(this.attributeToTrait.size());
    }

    /*
    private AttributeHandle createHLAPrivilegeToDeleteObjectAttribute() throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        AttributeHandle attributeHandle = null;
        try {
            attributeHandle = rtiAmbassador.getAttributeHandle(this.handle,"HLAprivilegeToDeleteObject");
        } catch (NameNotFound | InvalidObjectClassHandle e) {
            logger.warn("The handle for the attribute HLAprivilegeToDeleteObject of the object class <{}> could not be retrieved from the RTI. Attribute ownership operations will not work correctly.", this.name);
        }

        return attributeHandle;
    }
     */

    private Map<Attribute, Trait> computeAttributeToTraitAssociation(Set<Trait> traits) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        Map<Attribute, Trait> map = new HashMap<>();

        for (Trait trait : traits) {
            String attributeName = trait.getAnnotatedName();

            try {
                AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(this.handle, attributeName);
                Attribute attribute = new Attribute(attributeName, attributeHandle);

                map.put(attribute, trait);
            } catch (InvalidObjectClassHandle e) {
                throw new RtiHandleAcquisitionException(e);
            } catch (NameNotFound e) {
                throw new RtiHandleAcquisitionException("The attribute <" + attributeName + "> is not defined for the HLA object class <" + this.name + ">.");
            }
        }

        return map;
    }

    void publishAttributes(String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.attributes.clear();

        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(attributeName);

            if (attribute != null && !attribute.published.get()) {
                attribute.published.set(true);
                this.attributes.add(attribute.handle);
            }
        }

        try {
            rtiAmbassador.publishObjectClassAttributes(this.handle, this.attributes);
        } catch (AttributeNotDefined | ObjectClassNotDefined e) {
            throw new RuntimeException(e);
        }



        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        logger.info("Published HLA object class <{}> attributes: {}.", this.name, loggableAttributeNames);
    }

    void unpublishAttributes(String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress {
        this.attributes.clear();

        try {
            if (attributeNames.length > 1) {
                rtiAmbassador.unpublishObjectClass(this.handle);
            } else {
                for (String attributeName : attributeNames) {
                    Attribute attribute = getAttribute(attributeName);

                    if (attribute != null && attribute.published.get()) {
                        attribute.published.set(false);
                        this.attributes.add(attribute.handle);
                    }
                }

                rtiAmbassador.unpublishObjectClassAttributes(this.handle, this.attributes);
            }
        }  catch (AttributeNotDefined | ObjectClassNotDefined e) {
            throw new RuntimeException(e);
        }



        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        logger.info("Unpublished HLA object class <{}> attributes: {}.", this.name, loggableAttributeNames);
    }

    void subscribeAttributes(String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.attributes.clear();

        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(attributeName);

            if (attribute != null && !attribute.subscribed.get()) {
                attribute.subscribed.set(true);
                this.attributes.add(attribute.handle);
            }
        }

        try {
            rtiAmbassador.subscribeObjectClassAttributes(this.handle, this.attributes);
        } catch (AttributeNotDefined | ObjectClassNotDefined e) {
            throw new RuntimeException(e);
        }

        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        logger.info("Subscribed HLA object class <{}> attributes: {}.", this.name, loggableAttributeNames);
    }

    void unsubscribeAttributes(String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.attributes.clear();

        try {
            if (attributeNames.length > 1) {
                rtiAmbassador.unsubscribeObjectClass(this.handle);
            } else {
                for (String attributeName : attributeNames) {
                    Attribute attribute = getAttribute(attributeName);

                    if (attribute != null && attribute.subscribed.get()) {
                        attribute.subscribed.set(false);
                        this.attributes.add(attribute.handle);
                    }
                }

                rtiAmbassador.unsubscribeObjectClassAttributes(this.handle, this.attributes);
            }
        }  catch (AttributeNotDefined | ObjectClassNotDefined e) {
            throw new RuntimeException(e);
        }

        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        logger.info("Unsubscribed HLA object class <{}> attributes: {}.", this.name, loggableAttributeNames);
    }

    private String getNamesInLoggableFormat(String... attributeNames) {
        StringBuilder sb = new StringBuilder("[ ");

        for (int i = 0; i < attributeNames.length; i++) {
            sb.append(attributeNames[i]);

            if (i != attributeNames.length - 1) {
                sb.append(", ");
            } else {
                sb.append(" ]");
            }
        }

        return sb.toString();
    }

    void provideUpdate(ObjectInstanceHandle instanceHandle, Object proxy, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress {
        // TODO

        try {
            rtiAmbassador.updateAttributeValues(instanceHandle, null, null);
        } catch (AttributeNotDefined e) {
            throw new RuntimeException(e);
        } catch (ObjectInstanceNotKnown e) {
            throw new RuntimeException(e);
        }
    }

    void reflectRemoteUpdate(HLAObjectManager2.ObjectInstance objectInstance, AttributeHandleValueMap attributeValues) {
        if (objectInstance.getProxy() == null) {
            Object proxy = createProxy();
            objectInstance.setProxy(proxy);
        }

        for (Map.Entry<AttributeHandle, byte[]> entry :  attributeValues.entrySet()) {
            Attribute attribute = getAttribute(entry.getKey());

            if (attribute != null) {
                Trait trait = this.attributeToTrait.get(attribute);
                Object[] traitValues = trait.decode(objectInstance.getProxy(), entry.getValue());

                objectInstance.notifyAllListeners(trait.getAnnotatedName(), traitValues[0], traitValues[1]);
            }
        }
    }

    private Attribute getAttribute(String attributeName) {
        return this.attributeToTrait.keySet()
                .stream()
                .filter(a -> a.name.equals(attributeName))
                .findFirst()
                .orElse(null);
    }

    private Attribute getAttribute(AttributeHandle attributeHandle) {
        return this.attributeToTrait.keySet()
                .stream()
                .filter(a -> a.handle.equals(attributeHandle))
                .findFirst()
                .orElse(null);
    }

    String getName() {
        return this.name;
    }

    ObjectClassHandle getHandle() {
        return this.handle;
    }

    Class<?> getProxyClass() {
        return this.proxyClass;
    }

    Object createProxy() {
        try {
            return this.proxyClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not create object to manage values for remote object instance of the HLA object class <" + this.name + ">.", e);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("The class <" + this.proxyClass + "> assigned to the HLA object class <" + this.name + "> may lack a public constructor that accepts zero arguments.", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("The constructor for " + this.proxyClass + "threw an exception during instantiation.", e);
        }
    }

    AttributeHandleSet getSubscribedAttributes() {
        this.attributes.clear();

        for (Attribute attribute : this.attributeToTrait.keySet()) {
            if (attribute.subscribed.get()) {
                this.attributes.add(attribute.handle);
            }
        }

        return this.attributes;
    }

    AttributeHandleSet getAttributeHandlesAsSet(String... attributeNames) {
        this.attributes.clear();

        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(attributeName);

            if (attribute != null) {
                this.attributes.add(attribute.handle);
            }
        }

        return this.attributes;
    }

    private static final class Attribute {

        private final String name;

        private final AttributeHandle handle;

        private final AtomicBoolean published;

        private final AtomicBoolean subscribed;

        Attribute(String name, AttributeHandle handle) {
            this.name = name;
            this.handle = handle;
            this.published = new AtomicBoolean(false);
            this.subscribed = new AtomicBoolean(false);
        }
    }

    static final class Builder {

        private ObjectClassHandle handle;

        private SKAnnotatedTypeParser2.Metadata objectMetadata;

        Builder withHandle(ObjectClassHandle handle) {
            this.handle = handle;
            return this;
        }

        Builder withMetadata(SKAnnotatedTypeParser2.Metadata objectMetadata) {
            this.objectMetadata = objectMetadata;
            return this;
        }

        HLAObjectClass2 build() throws FederateNotExecutionMember, NotConnected, RTIinternalError {
            if (this.handle == null || this.objectMetadata == null) {
                throw new InternalObjectBuilderException("Missing one or more arguments required to initialize internal framework object representing an HLA object class.");
            }

            return new HLAObjectClass2(this);
        }
    }
}
