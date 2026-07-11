package org.see.skf.runtime;

import hla.rti1516_2025.encoding.EncoderFactory;
import org.see.skf.core.SKUtilityFactory;
import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.Coder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SKAnnotatedTypeParser {

    // All coders used by the federate are cached here, so that eventually we won't have to instantiate any new coders (which can be a computationally expensive process, since it uses the reflection API).
    private final Map<Class<? extends Coder<?>>, Coder<?>> coderPool;

    public SKAnnotatedTypeParser() {
        this.coderPool = new ConcurrentHashMap<>();
    }

    ParseResult parse(Object parseableObject) throws AnnotationParseException {
        return new ParseResult(parseableObject);
    }

    final class ParseResult {

        private final Object targetObject;

        // Name in the HLA FOM such as "HLAobjectRoot.PhysicalEntity" or "HLAinteractionRoot.ModeTransitionRequest".
        private final String fomClassName;

        // Each attribute/parameter corresponding to the Java field of the class.
        // For instance, we may have a class such as PhysicalEntity with the fields parentReferenceFrame and bodyWrtStructural.
        // In the FOM, these are designated as "parent_reference_frame" and "body_wrt_structural". So this mapping reconciles that difference.
        private final Map<String, Field> fomElementToField;

        // As per the enforced JavaBeans standard requirement for all simulation class models, there has to be a getter and setter for every field
        // that holds data for its corresponding attribute/parameter in the FOM.
        // Method[0] = getter and Method[1] = setter
        private final Map<Field, Method[]> fieldToGetterSetter;

        private final Map<Field, Coder<?>> fieldToCoder;

        ParseResult(Object object) {
            this.targetObject = object;

            ObjectClass objectClass = object.getClass().getAnnotation(ObjectClass.class);
            InteractionClass interactionClass = object.getClass().getAnnotation(InteractionClass.class);
            checkAnnotationSuitability(objectClass, interactionClass);

            if (objectClass != null) {
                this.fomClassName = objectClass.name();
            } else {
                this.fomClassName = interactionClass.name();
            }

            this.fomElementToField = new HashMap<>();
            this.fieldToGetterSetter = new HashMap<>();
            this.fieldToCoder = new HashMap<>();

            build();
        }

        private void checkAnnotationSuitability(ObjectClass objectClass, InteractionClass interactionClass) {
            if (objectClass != null && interactionClass != null) {
                throw new AnnotationParseException("<" + targetObject + "> has both an @ObjectClass or @InteractionClass annotation attached.");
            } else if (objectClass == null && interactionClass == null) {
                throw new AnnotationParseException("<" + targetObject + "> because has neither @ObjectClass nor @InteractionClass annotation attached.");
            }
        }

        private void build() {
            Class<?> targetClass = targetObject.getClass();

            while (targetClass != Object.class && targetClass.getAnnotation(ObjectClass.class) != null) {
                processClass(targetClass);

                // Continuously move up the object hierarchy.
                targetClass = targetClass.getSuperclass();
            }
        }

        private void processClass(Class<?> clazz) {
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(Attribute.class)) {
                    Attribute objectClassAttribute = field.getAnnotation(Attribute.class);
                    String attributeName = objectClassAttribute.name();

                    Method[] accessorMethods = new Method[]{
                            retrieveAccessor(clazz, attributeName, "get"),
                            retrieveAccessor(clazz, attributeName, "set")
                    };

                    Coder<?> attributeCoder = getAttributeCoder(objectClassAttribute.coder());

                    this.fomElementToField.put(attributeName, field);
                    this.fieldToGetterSetter.put(field, accessorMethods);
                    this.fieldToCoder.put(field, attributeCoder);
                }
            }
        }

        private Coder<?> getAttributeCoder(Class<? extends Coder<?>> clazz) {
            Coder<?> coderInstance;

            if (coderPool.containsKey(clazz)) {
                coderInstance = coderPool.get(clazz);
            } else {
                try {
                    EncoderFactory encoderFactory = SKUtilityFactory.INSTANCE.getEncoderFactory();
                    coderInstance = clazz.getDeclaredConstructor(EncoderFactory.class).newInstance(encoderFactory);

                    coderPool.put(clazz, coderInstance);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new AnnotationParseException("Problem instantiating the coder <" + clazz + ">. Ensure that a public constructor that accepts EncoderFactory as parameter is present.", e);
                } catch (InvocationTargetException e) {
                    throw new AnnotationParseException("Problem instantiating the coder <" + clazz + ">. An unexpected exception was thrown by its constructor.", e);
                }
            }

            return coderInstance;
        }

        // Prefix will only ever be "get" or "set".
        private Method retrieveAccessor(Class<?> clazz, String attributeName, String prefix) {
            String accessorName = prefix + capitalize(attributeName);

            try {
                return clazz.getDeclaredMethod(accessorName);
            } catch (NoSuchMethodException e) {
                throw new AnnotationParseException("No public " + prefix + "ter method for the attribute <" + attributeName + ">.");
            }
        }

        private String capitalize(String word) {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }

        String getFomClassName() {
            return this.fomClassName;
        }

        Map<String, Field> getFomElementFields() {
            return this.fomElementToField;
        }

        Map<Field, Method[]> getFieldMethods() {
            return this.fieldToGetterSetter;
        }

        Map<Field, Coder<?>> getFieldCoders() {
            return this.fieldToCoder;
        }
    }
}
