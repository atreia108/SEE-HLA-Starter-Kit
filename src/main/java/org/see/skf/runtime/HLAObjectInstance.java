package org.see.skf.runtime;

import hla.rti1516_2025.AttributeHandleValueMap;
import hla.rti1516_2025.ObjectInstanceHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.SKUtilityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class HLAObjectInstance {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectInstance.class);

    private final RTIambassador rtiAmbassador;

    private final String name;

    private final HLAObjectClass objectClass;

    private final ObjectInstanceHandle handle;

    private final Object instance;

    private final Map<SKAnnotatedTypeParser.Trait, Boolean> ownedTraits;

    private final Map<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> traitToClassAttribute;


    private HLAObjectInstance(String name, HLAObjectClass objectClass, ObjectInstanceHandle handle, Object instance, Map<SKAnnotatedTypeParser.Trait, Boolean> ownedTraits) {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        this.name = name;
        this.objectClass = objectClass;
        this.handle = handle;
        this.instance = instance;
        this.ownedTraits = ownedTraits;

        this.traitToClassAttribute = setupTraitToAttributeAssociation(ownedTraits.keySet());
    }

    private Map<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> setupTraitToAttributeAssociation(Set<SKAnnotatedTypeParser.Trait> traits) {
        Map<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> map = new HashMap<>();

        traits.forEach(trait -> {
            if (this.objectClass.getAttribute(trait.getName()) != null) {
                map.put(trait, this.objectClass.getAttribute(trait.getName()));
            }
        });

        return map;
    }

    AttributeHandleValueMap serialize(String... attributeNames) throws ObjectInstanceUpdateException {
        try {
            AttributeHandleValueMap map = rtiAmbassador.getAttributeHandleValueMapFactory().create(attributeNames.length);

            for (String attributeName : attributeNames) {
                Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> traitToAttribute = getTraitToAttributeEntry(attributeName);

                if (traitToAttribute != null) {
                    SKAnnotatedTypeParser.Trait trait = traitToAttribute.getKey();
                    HLAObjectClass.Attribute attribute = traitToAttribute.getValue();

                    map.put(attribute.getHandle(), trait.encode());
                } else {
                    throw new ObjectInstanceUpdateException("The attribute <" + attributeName + " and its associated serialization information is unknown for the object instance <" + this.name + ">.");
                }
            }

            return map;
        } catch (FederateNotExecutionMember | NotConnected e) {
            throw new ObjectInstanceUpdateException(e);
        }
    }

    private Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> getTraitToAttributeEntry(String attributeName) {
        for (Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> entry : traitToClassAttribute.entrySet()) {
            SKAnnotatedTypeParser.Trait t = entry.getKey();

            if (t.getName().equals(attributeName)) {
                return entry;
            }
        }

        return null;
    }

    // TODO
    void deserialize(AttributeHandleValueMap attributeValueMap) {

    }

    public void updateAttributes(String... attributeNames) throws ObjectInstanceUpdateException {
        AttributeHandleValueMap valueMapHandle = serialize(attributeNames);

        try {
            rtiAmbassador.updateAttributeValues(this.handle, valueMapHandle, null);
            String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
            logger.debug("Dispatched attribute updates for instance <{}>: {}", this.name, loggableAttributeNames);
        } catch (AttributeNotOwned | AttributeNotDefined | ObjectInstanceNotKnown | SaveInProgress | RestoreInProgress |
                 FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new ObjectInstanceUpdateException("The object instance <" + this.name +"> could not be updated.", e);
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

    // TODO
    void ownAttribute(String attributeName) {

    }

    // TODO
    void disownAttribute(String attributeName) {

    }

    public String getName() {
        return this.name;
    }

    public ObjectInstanceHandle getHandle() {
        return this.handle;
    }

    public Object getInstance() {
        return this.instance;
    }

    public static final class Builder {

        private String name;

        private HLAObjectClass objectClass;

        private ObjectInstanceHandle handle;

        private Object instance;

        private Map<SKAnnotatedTypeParser.Trait, Boolean> attributesToOwnership;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withHandle(ObjectInstanceHandle handle) {
            this.handle = handle;
            return this;
        }

        public Builder withObjectClass(HLAObjectClass objectClass) {
            this.objectClass = objectClass;
            return this;
        }

        public Builder withAttributes(Map<SKAnnotatedTypeParser.Trait, Boolean> classAttributesToOwnership) {
            this.attributesToOwnership = classAttributesToOwnership;
            return this;
        }

        public Builder forObject(Object object) {
            this.instance = object;
            return this;
        }

        public HLAObjectInstance build() throws ObjectInstanceCreationException {
            if (this.name == null || this.handle == null || this.objectClass == null || this.attributesToOwnership == null || this.instance == null) {
                throw new ObjectInstanceCreationException("One or more arguments needed for creating the object instance was not supplied.");
            }

            return new HLAObjectInstance(name, objectClass, handle, instance, attributesToOwnership);
        }
    }
}
