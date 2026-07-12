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

        HLAObjectClass objectClass;
        if ((objectClass = getObjectClass(name)) == null) {
            objectClass = new HLAObjectClass(name);
            this.objectClasses.add(objectClass);
        }

        objectClass.publish(attributeNames);
    }

    public void unpublishObjectClass(String name, String... attributes) {

    }

    public void subscribeObjectClass(String name, String... attributes) {

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
