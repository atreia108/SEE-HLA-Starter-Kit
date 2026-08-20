package org.see.skf.internal.runtime;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.internal.HLAUtilityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public final class HLAObjectInstance {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectInstance.class);

    private final RTIambassador rtiAmbassador;

    private final String name;

    private final ObjectInstanceHandle handle;

    private final HLAObjectClass objectClass;

    private PropertyChangeSupport propertyChangeSupport;

    private Object instance;

    private Map<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> traitToClassAttribute;

    private HLAObjectInstance(Builder builder) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.name = builder.name;
        this.handle = builder.handle;
        this.objectClass = builder.objectClass;
        this.instance = builder.instance != null ? builder.instance : null;
        this.traitToClassAttribute = builder.traits != null ? computeTraitToAttributeAssociation(builder.traits) : null;
    }

    private Map<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> computeTraitToAttributeAssociation(Set<SKAnnotatedTypeParser.Trait> traits) {
        Map<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> associations = new ConcurrentHashMap<>();

        if (traits != null) {
            Function<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> associationFunction = trait -> this.objectClass.getAttribute(trait.getName());
            traits.forEach(t -> associations.computeIfAbsent(t, associationFunction));
        }

        return associations;
    }

    private Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> getTraitToAttributeEntry(Predicate<Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute>> predicate) {
        return this.traitToClassAttribute.entrySet().stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    AttributeHandleValueMap serialize(AttributeHandleValueMap map, String... attributeNames) {
        for (String attributeName : attributeNames) {
            Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> traitToAttribute = getTraitToAttributeEntry(entry -> entry.getValue().getName().equals(attributeName));

            if (traitToAttribute != null) {
                SKAnnotatedTypeParser.Trait trait = traitToAttribute.getKey();
                HLAObjectClass.Attribute attribute = traitToAttribute.getValue();

                map.put(attribute.getHandle(), trait.encode());
            } else {
                throw new ObjectInstanceUpdateException("The attribute <" + attributeName + "> and its associated serialization information is unknown for the object instance <" + this.name + ">.");
            }
        }

        return map;
    }

    void deserialize(AttributeHandleValueMap attributeHandleValueMap) {
        if (isTrackable() && !this.traitToClassAttribute.isEmpty() && !attributeHandleValueMap.isEmpty()) {
           attributeHandleValueMap.forEach((attributeHandle,byteValue) -> {
               Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> traitToAttribute = getTraitToAttributeEntry(entry -> entry.getValue().getHandle().equals(attributeHandle));

               if (traitToAttribute != null) {
                   SKAnnotatedTypeParser.Trait trait = traitToAttribute.getKey();
                   Object[] values = trait.decode(byteValue);

                   propertyChangeSupport.firePropertyChange(trait.getName(), values[0], values[1]);
               }
           });
        }
    }

    void updateAttributes(AttributeHandleValueMap map, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, AttributeNotOwned {
        AttributeHandleValueMap valueMapHandle = serialize(map, attributeNames);

        try {
            rtiAmbassador.updateAttributeValues(this.handle, valueMapHandle, null);
            String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
            logger.debug("Dispatched updates for object instance <{}> attributes: {}", this.name, loggableAttributeNames);
        } catch (AttributeNotDefined e) {
            // The object instance should, in theory, have valid attribute serialization and handle information.
            // Therefore, this exception is highly unlikely to be thrown since the framework performs all the necessary validation during object class/instance creation.
            throw new ObjectInstanceUpdateException("One or more undefined attributes exist for this model using the HLA object class <" + this.name + ">.");
        } catch (ObjectInstanceNotKnown ignore) {
            logger.error("Updates not sent for the object instance <{}>. It may have been previously deleted.", this.name);
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

    public HLAObjectClass getObjectClass() {
        return this.objectClass;
    }

    public Object getInstance() {
        return this.instance;
    }

    void makeTrackable(Object instance, Set<SKAnnotatedTypeParser.Trait> traits) {
        if (this.instance == null) {
            this.instance = instance;
            this.propertyChangeSupport = new PropertyChangeSupport(instance);
            this.traitToClassAttribute = computeTraitToAttributeAssociation(traits);
        }
    }

    boolean isTrackable() {
        return this.instance != null;
    }

    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (propertyName != null) {
            this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        } else {
            this.propertyChangeSupport.addPropertyChangeListener(listener);
        }
    }

    void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (propertyName != null) {
            this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        } else {
            this.propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }

    public static final class Builder {

        private String name;

        private HLAObjectClass objectClass;

        private ObjectInstanceHandle handle;

        private Object instance;

        private Set<SKAnnotatedTypeParser.Trait> traits;

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

        public Builder withAttributes(Set<SKAnnotatedTypeParser.Trait> traits) {
            this.traits = traits;
            return this;
        }

        public Builder forObject(Object object) {
            this.instance = object;
            return this;
        }

        public HLAObjectInstance build() {
            if (this.name == null || this.handle == null || this.objectClass == null) {
                throw new IllegalStateException("One or more arguments needed for creating the object instance was not supplied.");
            }

            return new HLAObjectInstance(this);
        }
    }
}
