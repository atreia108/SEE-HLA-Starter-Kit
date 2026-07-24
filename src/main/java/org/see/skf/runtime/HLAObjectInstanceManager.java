package org.see.skf.runtime;

import hla.rti1516_2025.RTIambassador;
import org.see.skf.core.HLACallbackManager;
import org.see.skf.core.SKUtilityFactory;

import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;

public final class HLAObjectInstanceManager {
    private final RTIambassador rtiAmbassador;

    private final HLACallbackManager callbackManager;
    private final Set<HLAObjectInstance> objectInstances;
    private final Map<String, HLAObjectInstance> nameToObjectInstance;
    private final Map<HLAObjectInstance, String> objectInstanceToName;

    public HLAObjectInstanceManager(HLACallbackManager callbackManager) {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();
        this.callbackManager = callbackManager;

        this.objectInstances = new CopyOnWriteArraySet<>();
        this.nameToObjectInstance = new ConcurrentHashMap<>();
        this.objectInstanceToName = new ConcurrentHashMap<>();
    }

    // Name reservation occurs automatically in this step should there be a non-null argument for name.
    public void registerObjectInstance(Object object, String name) throws ObjectInstanceCreationException {
        if (name == null) {
            throw new ObjectInstanceCreationException("Name for object instance cannot be null.");
        }

        // TODO - Object instance creation logic.
        Future<Boolean> result = callbackManager.invokeNameReservationCallback(name);
    }

    public void registerObjectInstance(Object object) throws ObjectInstanceCreationException {

    }

    // Delete an owned object instance.
    // TODO - Add an option to relinquish name reservation upon deletion.
    public void deleteObjectInstance(Object object) {

    }

    // Remote object instance discovered.
    public void objectInstanceDiscovered() {

    }

    // Remove an unowned object instance upon instruction from the RTI.
    public void objectInstanceRemoved(Object object) {

    }

    public HLAObjectInstance getObjectInstance(String name) {
        // TODO - Return by name
        return null;
    }

    public void updateObjectInstance(Object object, String... attributes) {

    }

    public void releaseName(String name) {
        // TODO - Note that releasing the name has consequences for the object instance that uses it. This MUST be taken into account upon the name being released.
    }
}
