package org.see.skf.runtime;

import hla.rti1516_2025.AttributeHandle;
import hla.rti1516_2025.AttributeHandleSet;
import hla.rti1516_2025.ObjectClassHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
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
            throw new RtiHandleRetrievalException("<" + name + "> is not a valid object class in the FOM for this federation execution. Re-check name element in the @ObjectClass annotation.");

        } catch (FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new RtiHandleRetrievalException(e);
        }

        this.name = name;
        this.attributes = new CopyOnWriteArraySet<>();
    }

    public void publish(String... attributeNames) throws FederateNotExecutionMember, NotConnected, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, RTIinternalError, SaveInProgress {
        Set<Attribute> publishableAttributes = new HashSet<>(attributeNames.length);

        for (String attributeName : attributeNames) {
            Attribute attribute = getAndCreateAttributeIfAbsent(attributeName);
            attribute.published.set(true);

            publishableAttributes.add(attribute);
        }

        AttributeHandleSet publishableAttributeHandles = assembleAttributeHandleSet(publishableAttributes);
        rtiAmbassador.publishObjectClassAttributes(this.handle, publishableAttributeHandles);
    }

    public void unpublish(String... attributeNames) {
        for (String attributeName : attributeNames) {
            Attribute attribute = getAndCreateAttributeIfAbsent(attributeName);
            attribute.published.set(false);
        }
    }

    public void subscribe(String ...attributeNames) {
        for (String attributeName : attributeNames) {
            Attribute attribute = getAndCreateAttributeIfAbsent(attributeName);
            attribute.subscribed.set(true);
        }
    }

    public void unsubscribe(String... attributeNames) {
        for (String attributeName : attributeNames) {
            Attribute attribute = getAndCreateAttributeIfAbsent(attributeName);
            attribute.subscribed.set(false);
        }
    }

    private void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }

    private Attribute getAndCreateAttributeIfAbsent(String attributeName) {
        Attribute targetAttribute =  this.attributes.stream()
                .filter(attribute -> attribute.name.equals(attributeName))
                .findFirst().orElseGet(() -> new Attribute(attributeName));

        addAttribute(targetAttribute);
        return targetAttribute;
    }

    private AttributeHandleSet assembleAttributeHandleSet(Set<Attribute> set) throws FederateNotExecutionMember, NotConnected {
        AttributeHandleSet handleSet = rtiAmbassador.getAttributeHandleSetFactory().create();

        set.forEach((attribute) -> {
            handleSet.add(attribute.handle);
        });

        return handleSet;
    }

    public String getName() {
        return this.name;
    }

    public ObjectClassHandle getHandle() {
        return this.handle;
    }

    final class Attribute {
        private final String name;
        private final AttributeHandle handle;
        private final AtomicBoolean published;
        private final AtomicBoolean subscribed;

        Attribute(String name) {
            this.name = name;
            this.published = new AtomicBoolean(false);
            this.subscribed = new AtomicBoolean(false);

            try {
                this.handle = rtiAmbassador.getAttributeHandle(HLAObjectClass.this.handle, name);
            } catch (NameNotFound e) {
                throw new RtiHandleRetrievalException("<" + this.name + "> is not a recognized attribute for the object class <" + HLAObjectClass.this.name + ">.", e);
            } catch (InvalidObjectClassHandle | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
                throw new RuntimeException(e);
            }
        }
    }
}
