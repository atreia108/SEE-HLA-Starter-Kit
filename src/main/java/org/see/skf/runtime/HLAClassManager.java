package org.see.skf.runtime;

import hla.rti1516_2025.RTIambassador;
import org.see.skf.core.SKUtilityFactory;
import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.ObjectClass;

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

    public void publishObjectClass(ObjectClass objectClass, String... attributes) {

    }

    public void unpublishObjectClass(ObjectClass objectClass, String... attributes) {

    }

    public void subscribeObjectClass(ObjectClass objectClass, String... attributes) {

    }

    public void unsubscribeObjectClass(ObjectClass objectClass, String... attributes) {

    }

    public void publishInteractionClass(InteractionClass interactionClass) {

    }

    public void unpublishInteractionClass(InteractionClass interactionClass) {

    }

    public void subscribeInteractionClass(InteractionClass interactionClass) {

    }

    public void unsubscribeInteractionClass(InteractionClass interactionClass) {

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
