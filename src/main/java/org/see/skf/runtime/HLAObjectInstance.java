package org.see.skf.runtime;

import hla.rti1516_2025.AttributeHandle;
import hla.rti1516_2025.AttributeHandleValueMap;
import hla.rti1516_2025.ObjectInstanceHandle;
import hla.rti1516_2025.RTIambassador;
import org.see.skf.core.SKUtilityFactory;
import org.see.skf.encoding.Coder;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class HLAObjectInstance {
    private final RTIambassador rtiAmbassador;

    private final String name;
    private final ObjectInstanceHandle handle;
    private final Map<HLAObjectClass.Attribute, AttributeReflectionData> attributeToReflectionData;
    private final Set<HLAObjectClass.Attribute> ownedAttributes;

    private final PropertyChangeSupport propertyChangeSupport;
    private final Set<PropertyChangeListener> observers;

    // private final Object instance;

    HLAObjectInstance(String name, Map<HLAObjectClass.Attribute, Boolean> attributeToOwnership, SKAnnotatedTypeParser.ParseResult objectReflectionData) {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        // TODO - ObjectInstanceHandle initialization here.
        this.handle = null;

        this.name = name;
        // this.instance = objectReflectionData.getTargetObject();
        this.attributeToReflectionData = new HashMap<>();
        this.ownedAttributes = new HashSet<>();

        this.observers = new CopyOnWriteArraySet<>();
        this.propertyChangeSupport = new PropertyChangeSupport(this);

        generateAttributes(attributeToOwnership, objectReflectionData);
    }

    private void generateAttributes(Map<HLAObjectClass.Attribute, Boolean> attributeToOwnership, SKAnnotatedTypeParser.ParseResult objectReflectionData) {
        attributeToOwnership.forEach((attribute, isOwned) -> {
            // Method[] accessors = objectReflectionData.getAttributeAccessors(attribute.getName());
            // Coder<?> coder = objectReflectionData.getAttributeCoder(attribute.getName());
            // Method[] coderMethods = objectReflectionData.getAttributeCoderMethods(coder);

            // AttributeReflectionData attributeReflectionData = new AttributeReflectionData(accessors, coder, coderMethods);
            // attributeToReflectionData.put(attribute, attributeReflectionData);

            if (Boolean.TRUE.equals(isOwned)) {
                this.ownedAttributes.add(attribute);
            }
        });
    }

    AttributeHandleValueMap serializeAttributes(String... attributeNames) {

        return null;
    }

    void deserializeAttributes(AttributeHandleValueMap newValueHandleMap) {
        newValueHandleMap.forEach((k,v) -> {
            HLAObjectClass.Attribute attribute = getAttribute(k);
            AttributeReflectionData reflectionData = this.attributeToReflectionData.get(attribute);

            String attributeName = attribute.getName();
            try {
                Object[] values = reflectionData.decode(v);
                Object oldValue = values[0];
                Object newValue = values[1];
                notifyObservers(attributeName, oldValue, newValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ObjectUpdateException("Exception occurred whilst decoding new values for the <" + attributeName + "> attribute of <" + this.name + ">.", e);
            }
        });
    }

    void attributeOwned(String attributeName) {
        this.ownedAttributes.stream()
                .filter(a -> a.getName().equals(attributeName))
                .findFirst()
                .ifPresent(this.ownedAttributes::add);
    }

    void attributeDisowned(String attributeName) {
        this.ownedAttributes.stream()
                .filter(a -> a.getName().equals(attributeName))
                .findFirst()
                .ifPresent(this.ownedAttributes::remove);
    }

    void registerObserver(PropertyChangeListener observer) {
        this.propertyChangeSupport.addPropertyChangeListener(observer);
    }

    void unregisterObserver(PropertyChangeListener observer) {
        this.propertyChangeSupport.removePropertyChangeListener(observer);
    }

    void notifyObservers(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    String getName() {
        return this.name;
    }

    HLAObjectClass.Attribute getAttribute(AttributeHandle handle) {
        return this.attributeToReflectionData.keySet().stream()
                .filter(a -> a.getHandle().equals(handle))
                .findFirst()
                .orElse(null);
    }

    /*
    Object get() {
        return this.instance;
    }
     */

    private final class AttributeReflectionData {

        private final Method getter;

        private final Method setter;

        private final Coder<?> coder;

        private final Method encode;

        private final Method decode;

        private AttributeReflectionData(Method[] accessorMethods, Coder<?> coder, Method[] coderMethods) {
            this.getter = accessorMethods[0];
            this.setter = accessorMethods[1];

            this.coder = coder;
            this.encode = coderMethods[0];
            this.decode = coderMethods[1];
        }

        byte[] encode() throws IllegalAccessException, InvocationTargetException {
            // Object value = getter.invoke(instance);
            // Object encodedValue = encode.invoke(coder, value);

            // return (byte[]) encodedValue;
            return null;
        }

        Object[] decode(byte[] encodedValue) throws IllegalAccessException, InvocationTargetException {
            // Perhaps you may be warned here that the following line is incorrect. Casting the second argument to
            // java.lang.Object makes the warning go away. Be wise, and do not heed its words. All is as it should be.
            // Retaining byte[] as the type for the parameter is, in fact, the correct choice - decoding won't work
            // otherwise.
            // Object oldValue = getter.invoke(instance);
            Object newValue = decode.invoke(coder, encodedValue);
            // setter.invoke(instance, newValue);

            // return new Object[] { oldValue, newValue };
            return null;
        }
    }
}
