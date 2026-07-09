package org.see.skf.runtime;

import hla.rti1516_2025.AttributeHandleValueMap;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.FederateNotExecutionMember;
import hla.rti1516_2025.exceptions.NotConnected;
import org.see.skf.core.SKUtilityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class HLAObjectInstance {
    private final Logger logger = LoggerFactory.getLogger(HLAObjectInstance.class);
    private final RTIambassador rtiAmbassador;

    private String name;
    private final HLAObjectClass objectClass;
    private final Object instance;
    private final Set<HLAObjectClassAttribute> attributes;
    private final Set<HLAObjectClassAttribute> ownedAttributes;
    private final AttributeHandleValueMap handleValueMap;

    // TODO - ACCOUNT FOR NAME RESERVATION.
    public HLAObjectInstance(HLAObjectClass objectClass, Object instance, String name) {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();
        this.instance = instance;
        this.objectClass = objectClass;
        this.attributes = objectClass.getAttributes();

        try {
            int attributeCount = attributes.size();
            this.handleValueMap = rtiAmbassador.getAttributeHandleValueMapFactory().create(attributeCount);
        } catch (FederateNotExecutionMember | NotConnected e) {
            String className = objectClass.getName();
            throw new RtiHandleRetrievalException("Could not create AttributeHandleValueMap for <" + className + ">.", e);
        }

        this.ownedAttributes = new CopyOnWriteArraySet<>();
    }

    public void decode() {

    }

    // TODO - Attribute ownership acquisition logic.
    public void acquireOwnership(String attributeName) {

    }

    // TODO - Attribute ownership release logic.
    public void releaseOwnership(String attributeName) {

    }

    // TODO - Now that we're allowing specific attributes to be updated, we have to provide which ones are going to be sent to the RTI.
    public void serialize(String... attributeNames) {
        // TODO - Encode the entire object instance for dispatch to RTI.
    }

    public void deserialize(AttributeHandleValueMap attributeValueMap) {
        // TODO - Write all changes into the object instance fields.
    }

    public Object get() {
        return this.instance;
    }
}
