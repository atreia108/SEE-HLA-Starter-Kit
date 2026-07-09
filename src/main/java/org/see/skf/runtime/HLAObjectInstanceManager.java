package org.see.skf.runtime;

import hla.rti1516_2025.RTIambassador;
import org.see.skf.core.SKUtilityFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class HLAObjectInstanceManager {
    private final RTIambassador rtiAmbassador;

    private final Set<HLAObjectInstance> objectInstances;
    private final Set<String> reservedNames;

    public HLAObjectInstanceManager() {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        this.objectInstances = new CopyOnWriteArraySet<>();
        this.reservedNames = new CopyOnWriteArraySet<>();
    }

    public void createObjectInstance(String name) {

    }

    // TODO - Add an option to relinquish name reservation upon deletion.
    public void destroyObjectInstance(HLAObjectInstance objectInstance) {
        this.objectInstances.remove(objectInstance);
    }

    public HLAObjectInstance getObjectInstance(String name) {
        // TODO - Return by name
        return null;
    }

    public void updateObjectInstance(String name) {

    }

    public void reserveName(String name) {
        // TODO - Acquire lock on a name from the RTI.
    }
}
