package org.see.skf.runtime;

import hla.rti1516_2025.AttributeHandle;
import hla.rti1516_2025.AttributeHandleSet;
import hla.rti1516_2025.ObjectClassHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.HLAClassDeclarationException;
import org.see.skf.core.SKUtilityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HLAObjectClass {

    private final Logger logger = LoggerFactory.getLogger(HLAObjectClass.class);

    private final RTIambassador rtiAmbassador;

    private final String name;
    private final ObjectClassHandle handle;
    private final Set<Attribute> attributes;

    public HLAObjectClass(String name) {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        try {
            this.handle = rtiAmbassador.getObjectClassHandle(name);
        } catch (NameNotFound e) {
            throw new RtiHandleException("<" + name + "> is not a valid object class in the FOM for this federation execution. Re-check name element in the @ObjectClass annotation.");

        } catch (FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new RtiHandleException(e);
        }

        this.name = name;
        this.attributes = new CopyOnWriteArraySet<>();
    }

    public void publish(String... attributeNames) throws HLAClassDeclarationException {
        Set<Attribute> publishedAttributes = new HashSet<>(attributeNames.length);

        for (String attributeName : attributeNames) {
            Attribute attribute = getAndCreateAttributeIfAbsent(attributeName);
            attribute.published.set(true);

            publishedAttributes.add(attribute);
        }

        AttributeHandleSet publishedAttributeHandles = assembleAttributeHandleSet(publishedAttributes);
        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        try {
            rtiAmbassador.publishObjectClassAttributes(this.handle, publishedAttributeHandles);
        } catch (AttributeNotDefined | ObjectClassNotDefined | SaveInProgress | RestoreInProgress |
                 FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new HLAClassDeclarationException("Could not publish these <" + this.name + "> attributes: " + loggableAttributeNames, e);
        }

        logger.info("Published <{}> attributes: {}", this.name, loggableAttributeNames);
    }

    // TODO
    public void unpublish(String... attributeNames) {
        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(attributeName);

            if (attribute != null) {
                attribute.published.set(false);
            } else {
                logger.warn("Could not unpublish the attribute <{}> of object class <{}> because it has not been published yet.", attributeName, this.name);
            }
        }
    }

    public void subscribe(String ...attributeNames) throws HLAClassDeclarationException {
        Set<Attribute> subscribedAttributes = new HashSet<>(attributeNames.length);

        for (String attributeName : attributeNames) {
            Attribute attribute = getAndCreateAttributeIfAbsent(attributeName);
            attribute.subscribed.set(true);

            subscribedAttributes.add(attribute);
        }

        AttributeHandleSet subscribedAttributeHandles = assembleAttributeHandleSet(subscribedAttributes);
        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        try {
            rtiAmbassador.subscribeObjectClassAttributes(this.handle, subscribedAttributeHandles);
        } catch (AttributeNotDefined | ObjectClassNotDefined | SaveInProgress | RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new HLAClassDeclarationException("Could not subscribe these <" + this.name + "> attributes: " + loggableAttributeNames, e);
        }

        logger.info("Subscribed <{}> attributes: {}", this.name, loggableAttributeNames);
    }

    // TODO
    public void unsubscribe(String... attributeNames) {
        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(attributeName);

            if (attribute != null) {
                attribute.subscribed.set(false);
            } else {
                logger.warn("Could not unsubscribe the attribute <{}> of object class <{}> because it has not been published yet.", attributeName, this.name);
            }
        }
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

    private Attribute getAndCreateAttributeIfAbsent(String attributeName) {
        Attribute targetAttribute =  this.attributes.stream()
                .filter(attribute -> attribute.name.equals(attributeName))
                .findFirst().orElseGet(() -> new Attribute(attributeName));

        this.attributes.add(targetAttribute);
        return targetAttribute;
    }

    private AttributeHandleSet assembleAttributeHandleSet(Set<Attribute> set) {
        try {
            AttributeHandleSet handleSet = rtiAmbassador.getAttributeHandleSetFactory().create();
            set.forEach(attribute -> handleSet.add(attribute.handle));

            return handleSet;
        } catch (FederateNotExecutionMember | NotConnected e) {
            throw new RtiHandleException("Could not create attribute handle set to store object class attributes.", e);
        }
    }

    public String getName() {
        return this.name;
    }

    public ObjectClassHandle getHandle() {
        return this.handle;
    }

    Attribute getAttribute(String name) {
        return attributes.stream()
                .filter(attribute -> attribute.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    Attribute getAttribute(AttributeHandle handle) {
        return attributes.stream()
                .filter(attribute -> attribute.handle == handle)
                .findFirst()
                .orElse(null);
    }

    final class Attribute {

        private final String name;

        private final AttributeHandle handle;

        private final AtomicBoolean published;

        private final AtomicBoolean subscribed;

        private Attribute(String name) {
            this.name = name;
            this.published = new AtomicBoolean(false);
            this.subscribed = new AtomicBoolean(false);

            try {
                this.handle = rtiAmbassador.getAttributeHandle(HLAObjectClass.this.handle, name);
            } catch (NameNotFound e) {
                throw new RtiHandleException("<" + this.name + "> is not a recognized attribute for the object class <" + HLAObjectClass.this.name + ">.", e);
            } catch (InvalidObjectClassHandle | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
                throw new RtiHandleException(e);
            }
        }

        String getName() {
            return this.name;
        }

        AttributeHandle getHandle() {
            return this.handle;
        }
    }
}
