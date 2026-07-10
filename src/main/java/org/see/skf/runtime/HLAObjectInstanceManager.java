package org.see.skf.runtime;

import hla.rti1516_2025.RTIambassador;
import org.see.skf.core.SKUtilityFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public final class HLAObjectInstanceManager {
    private final RTIambassador rtiAmbassador;

    private final Set<HLAObjectInstance> objectInstances;
    private final Map<String, HLAObjectInstance> nameToObjectInstance;

    public HLAObjectInstanceManager() {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        this.objectInstances = new CopyOnWriteArraySet<>();
        this.nameToObjectInstance = new ConcurrentHashMap<>();
    }

    // Name reservation occurs automatically in this step should there be a non-null argument for name.
    public void createObjectInstance(Object object, String name) {
        if (name != null) {
            reserveName(name);
        }


    }

    // TODO - Add an option to relinquish name reservation upon deletion.
    public void destroyObjectInstance(Object object) {

    }

    public HLAObjectInstance getObjectInstance(String name) {
        // TODO - Return by name
        return null;
    }

    public void updateObjectInstance(Object object, String... attributes) {

    }

    public void reserveName(String name) {

    }

    public void releaseName(String name) {
        // TODO - Note that releasing the name has consequences for the object instance that uses it. This MUST be taken into account upon the name being released.
    }
}
