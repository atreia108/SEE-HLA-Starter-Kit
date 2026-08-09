package org.see.skf.internal.runtime;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.HLAUtilityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class HLAObjectInstance {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectInstance.class);

    private final RTIambassador rtiAmbassador;

    private final String name;

    private final ObjectInstanceHandle handle;

    private final HLAObjectClass objectClass;

    private final AttributeHandleValueMap cachedAttributeHandleValueMap;

    private final AttributeHandleSet cachedAttributeHandleSet;

    private PropertyChangeSupport propertyChangeSupport;

    private Object instance;

    private final Map<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> traitToClassAttribute;

    private HLAObjectInstance(Builder builder) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.name = builder.name;
        this.handle = builder.handle;
        this.objectClass = builder.objectClass;
        this.instance = builder.instance != null ? builder.instance : null;

        int count = this.objectClass.getAllAttributes().size();
        this.cachedAttributeHandleValueMap = createAttributeHandleValueMap(count);
        this.cachedAttributeHandleSet = createAttributeHandleSet();

        this.traitToClassAttribute = new ConcurrentHashMap<>();
        computeTraitToAttributeAssociation(builder.traits);
    }

    private AttributeHandleValueMap createAttributeHandleValueMap(int size) {
        try {
            return this.rtiAmbassador.getAttributeHandleValueMapFactory().create(size);
        } catch (FederateNotExecutionMember | NotConnected e) {
            throw new RuntimeException("Could not create AttributeHandleValueMap for packing attribute values in the object instance <" + this.name + ">.", e);
        }
    }

    private AttributeHandleSet createAttributeHandleSet() {
        try {
            return this.rtiAmbassador.getAttributeHandleSetFactory().create();
        } catch (FederateNotExecutionMember | NotConnected e) {
            throw new RuntimeException("Could not create AttributeHandleSet in the object instance <" + this.name + ">.", e);
        }
    }

    private void computeTraitToAttributeAssociation(Set<SKAnnotatedTypeParser.Trait> traits) {
        if (traits != null) {
            Function<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> associationFunction = trait -> this.objectClass.getAttribute(trait.getName());
            traits.forEach(t -> this.traitToClassAttribute.computeIfAbsent(t, associationFunction));
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

    private Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> getTraitToAttributeEntry(AttributeHandle attributeHandle) {
        for (Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> entry : traitToClassAttribute.entrySet()) {
            HLAObjectClass.Attribute attribute = entry.getValue();

            if (attributeHandle.equals(attribute.getHandle())) {
                return entry;
            }
        }

        return null;
    }

    AttributeHandleValueMap serialize(String... attributeNames) throws ObjectInstanceUpdateException {
        this.cachedAttributeHandleValueMap.clear();

        for (String attributeName : attributeNames) {
            Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> traitToAttribute = getTraitToAttributeEntry(attributeName);

            if (traitToAttribute != null) {
                SKAnnotatedTypeParser.Trait trait = traitToAttribute.getKey();
                HLAObjectClass.Attribute attribute = traitToAttribute.getValue();

                this.cachedAttributeHandleValueMap.put(attribute.getHandle(), trait.encode());
            } else {
                throw new ObjectInstanceUpdateException("The attribute <" + attributeName + "> and its associated serialization information is unknown for the object instance <" + this.name + ">.");
            }
        }

        return this.cachedAttributeHandleValueMap;
    }

    private void deserialize() {
        if (this.instance != null && !this.traitToClassAttribute.isEmpty()) {
           this.cachedAttributeHandleValueMap.forEach((attributeHandle,byteValue) -> {
               Map.Entry<SKAnnotatedTypeParser.Trait, HLAObjectClass.Attribute> traitToAttribute = getTraitToAttributeEntry(attributeHandle);

               if (traitToAttribute != null) {
                   SKAnnotatedTypeParser.Trait trait = traitToAttribute.getKey();
                   Object[] values = trait.decode(byteValue);

                   propertyChangeSupport.firePropertyChange(trait.getName(), values[0], values[1]);
               }
           });
        }
    }

    public void updateAttributes(String... attributeNames) throws ObjectInstanceUpdateException {
        AttributeHandleValueMap valueMapHandle = serialize(attributeNames);

        try {
            rtiAmbassador.updateAttributeValues(this.handle, valueMapHandle, null);
            String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
            logger.debug("Dispatched updates for instance <{}> attributes: {}", this.name, loggableAttributeNames);
        } catch (AttributeNotOwned | AttributeNotDefined | ObjectInstanceNotKnown | SaveInProgress | RestoreInProgress |
                 FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new ObjectInstanceUpdateException("Updates for the object instance <" + this.name +"> were not sent.", e);
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

    /*
    public AttributeHandleSet getAttributeHandles(Set<String> attributeNames) {
        this.cachedAttributeHandleSet.clear();

        attributeNames.forEach(attributeName -> {
            AttributeHandle attributeHandle = getAttributeHandle(attributeName);

            if (this.handle != null) {
                this.cachedAttributeHandleSet.add(attributeHandle);
            } else {
                throw new NoSuchAttributeException("The attribute <" + attributeName +"> has not been internally generated for the object instance <" + this.name + ">.");
            }
        });

        return this.cachedAttributeHandleSet;
    }

    private AttributeHandle getAttributeHandle(String attributeName) {
        for (HLAObjectClass.Attribute attribute : this.traitToClassAttribute.values()) {
            if (attribute.getName().equals(attributeName)) {
                return attribute.getHandle();
            }
        }

        return null;
    }
     */

    public String getName() {
        return this.name;
    }

    public ObjectInstanceHandle getHandle() {
        return this.handle;
    }

    public Object getInstance() {
        return this.instance;
    }

    void setInstance(Object instance) {
        if (this.instance == null) {
            this.instance = instance;
            this.propertyChangeSupport = new PropertyChangeSupport(instance);
            deserialize();
        }
    }

    void setTraits(Set<SKAnnotatedTypeParser.Trait> traits) {
        computeTraitToAttributeAssociation(traits);
        deserialize();
    }

    void setCachedAttributeHandleValueMap(AttributeHandleValueMap attributeHandleValueMap) {
        this.cachedAttributeHandleValueMap.clear();
        this.cachedAttributeHandleValueMap.putAll(attributeHandleValueMap);
        deserialize();
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
