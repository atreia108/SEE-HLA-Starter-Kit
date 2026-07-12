package org.see.skf.runtime;

import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.SKUtilityFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class HLAClassManager {
    private final RTIambassador rtiAmbassador;

    private final Set<HLAObjectClass> objectClasses;
    private final Set<HLAInteractionClass> interactionClasses;

    public HLAClassManager() {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        this.objectClasses = new CopyOnWriteArraySet<>();
        this.interactionClasses = new CopyOnWriteArraySet<>();
    }

    public void publishObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, NotConnected, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, RTIinternalError, SaveInProgress {
        if (attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute is required to publish the object class <" + name + ">.");
        }

        HLAObjectClass objectClass = getAndCreateObjectClassIfAbsent(name);
        objectClass.publish(attributeNames);
    }

    public void unpublishObjectClass(String name, String... attributes) {

    }

    public void subscribeObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute is required to subscribe the object class <" + name + ">.");
        }

        HLAObjectClass objectClass = getAndCreateObjectClassIfAbsent(name);
        objectClass.subscribe(attributeNames);
    }

    public void unsubscribeObjectClass(String name, String... attributes) {

    }

    public void publishInteractionClass(String name) {

    }

    public void unpublishInteractionClass(String name) {

    }

    public void subscribeInteractionClass(String name) {

    }

    public void unsubscribeInteractionClass(String name) {

    }

    private HLAObjectClass getAndCreateObjectClassIfAbsent(String name) {
        HLAObjectClass objectClass = this.objectClasses.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseGet(() -> new HLAObjectClass(name));

        this.objectClasses.add(objectClass);
        return objectClass;
    }

    public HLAObjectClass getObjectClass(String name) {
        return objectClasses.stream()
                .filter(objectClass -> objectClass.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public HLAInteractionClass getInteractionClass(String name) {
        return interactionClasses.stream()
                .filter(interactionClass -> interactionClass.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
