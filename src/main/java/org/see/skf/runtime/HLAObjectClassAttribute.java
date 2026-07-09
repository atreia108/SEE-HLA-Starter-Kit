package org.see.skf.runtime;

import hla.rti1516_2025.AttributeHandle;
import hla.rti1516_2025.ObjectClassHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.SKUtilityFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public final class HLAObjectClassAttribute {

    private final RTIambassador rtiAmbassador;

    private final String name;
    private final AttributeHandle handle;
    private final AtomicBoolean published;
    private final AtomicBoolean subscribed;

    // TODO - Update all of this.
    public HLAObjectClassAttribute(String className, String attributeName, boolean owned) {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();
        this.handle = null;

        this.name = attributeName;
        this.published = new AtomicBoolean(false);
        this.subscribed = new AtomicBoolean(false);
    }

    /*
    public HLAObjectClassAttribute(HLAObjectClass objectClass, String name, boolean owned) {
        RTIambassador rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        try {
            ObjectClassHandle classHandle = objectClass.getHandle();
            this.handle = rtiAmbassador.getAttributeHandle(classHandle, name);
        } catch (NameNotFound e) {
            String className = objectClass.getName();
            throw new RtiHandleRetrievalException("<" + name + "> is not a valid attribute for the object class <" + className + ">. Re-check name element in the @Attribute annotation.");
        } catch (InvalidObjectClassHandle | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new RtiHandleRetrievalException(e);
        }

        this.name = name;
        this.published = new AtomicBoolean(false);
        this.subscribed = new AtomicBoolean(false);
    }
     */

    public void published(boolean flag) {
        this.published.set(flag);
    }

    public void subscribed(boolean flag) {
        this.subscribed.set(flag);
    }

    public String getName() {
        return this.name;
    }

    public AttributeHandle getHandle() {
        return this.handle;
    }

    static class Parser {

    }
}